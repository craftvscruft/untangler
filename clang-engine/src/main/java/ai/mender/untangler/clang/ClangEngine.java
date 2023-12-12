package ai.mender.untangler.clang;

import ai.mender.untangler.shared.Ast;
import ai.mender.untangler.shared.SourcePosition;
import ai.mender.untangler.shared.SourceRange;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

public class ClangEngine {

    public static final String KIND_DEF_VAR = "def:var";
    public static final String KIND_DEF_FUNCTION = "def:fn";
    public static final String KIND_PARAM = "param";
    public static final String KIND_REF = "ref";
    public static final String KIND_DEF_TYPE = "def:type";
    public static final String KIND_DEF_FIELD = "def:field";
    public static final String PROP_NAME = "name";
    public static final String PROP_ID = "id";
    public static final String PROP_REF_ID = "refId";
    public static final String PROP_TYPE = "type";

    static class Transformer {
        private final Map<String, String> watchedKinds;
        private int curLine;
        private boolean debug = false;

        public Transformer(Map<String, String> watchedKinds) {
            this.watchedKinds = watchedKinds;
            this.curLine = -1;
        }

        public void visit(JsonNode node, Ast resultTree) {
            if (node.has("isImplicit") && node.get("isImplicit").asBoolean(false)) {
                return;
            }
            JsonNode locLineNode = node.at("/loc/line");
            if (!locLineNode.isMissingNode()) {
                curLine = locLineNode.asInt();
            }
            if (node.has("range")) {
                JsonNode beginLineNode = node.at("/range/begin/line");
                JsonNode beginLineNode2 = node.at("/range/begin/expansionLoc/line");
                JsonNode beginColNode = node.at("/range/begin/col");
                JsonNode beginColNode2 = node.at("/range/begin/expansionLoc/col");

                JsonNode locColNode = node.at("/loc/col");
                JsonNode locTokLenNode = node.at("/loc/tokLen");
                SourceRange nameRange = null;
                if (locColNode.isInt() && locTokLenNode.isInt()) {
                    nameRange = new SourceRange(
                            new SourcePosition(curLine, locColNode.asInt()),
                            new SourcePosition(curLine, locColNode.asInt() + locTokLenNode.asInt()));
                }

                int beginLineBackup = beginLineNode2.isMissingNode() ? curLine : beginLineNode2.asInt();
                curLine = beginLineNode.isMissingNode() ? beginLineBackup : beginLineNode.asInt();
    
                int beginColBackup = beginColNode2.isMissingNode() ? -1 : beginColNode2.asInt();
                int beginCol = beginColNode.isMissingNode() ? beginColBackup : beginColNode.asInt();
    
                JsonNode endLineNode = node.at("/range/end/line");
                JsonNode endLineNode2 = node.at("/range/end/expansionLoc/line");
                JsonNode endColNode = node.at("/range/end/col");
                JsonNode endColNode2 = node.at("/range/end/expansionLoc/col");
                JsonNode endColTokLenNode = node.at("/range/end/tokLen");
                JsonNode endColTokLenNode2 = node.at("/range/end/expansionLoc/tokLen");
                //TODO: Range is the whole statement, but loc is the name. We need both, at least some times.
                //"loc": {
                //    "offset": 554,
                //     "line": 30,
                //     "col": 3,
                //     "tokLen": 1
                //},



                int endLineBackup = endLineNode2.isMissingNode() ? curLine : endLineNode2.asInt();
                int endLine = endLineNode.isMissingNode() ? endLineBackup : endLineNode.asInt();
    
                int endColBackup = endColNode2.isMissingNode() ? -1 : endColNode2.asInt();
                int endCol = endColNode.isMissingNode() ? endColBackup : endColNode.asInt();

                int endTokLenBackup = endColTokLenNode2.isMissingNode() ? -1 : endColTokLenNode2.asInt();
                int endTokLen = endColTokLenNode.isMissingNode() ? endTokLenBackup : endColTokLenNode.asInt();

                String id = node.path(PROP_ID).asText(null);
                String name = node.path(PROP_NAME).asText(null);
                SourcePosition start = new SourcePosition(curLine, beginCol);
                SourcePosition end = new SourcePosition(endLine, endCol + endTokLen);
                SourceRange range = new SourceRange(start, end);
                String kind = node.path("kind").asText("UNKNOWN");
                JsonNode typeNode = node.get(PROP_TYPE);
                ClangType type = parseTypeNode(typeNode);
                ClangRef ref = null;
                if (node.has("referencedDecl")) {
                    JsonNode refNode = node.get("referencedDecl");
                    String refName = refNode.path(PROP_NAME).asText("UNKNOWN");
                    String refKind = refNode.path("kind").asText("UNKNOWN");
                    String refId = refNode.path(PROP_ID).asText("UNKNOWN");
                    ref = new ClangRef(refId, refName, refKind, parseTypeNode(refNode.get(PROP_TYPE)));
                }

                if (watchedKinds.containsKey(kind)) {
                    if (debug) {
                        System.out.println(new ClangAnnotation(kind, range, type, ref));
                    }
                    Ast newAstNode = Ast.create(watchedKinds.get(kind), range, nameRange);
                    resultTree.children().add(newAstNode);
                    if (name != null) {
                        newAstNode.props().put(PROP_NAME, name);
                    }
                    if (id != null) {
                        newAstNode.props().put(PROP_ID, id);
                    }
                    if (ref != null) {
                        newAstNode.props().put(PROP_REF_ID, ref.id);
                        if (name == null) {
                            newAstNode.props().put(PROP_NAME, ref.name);
                        }
                    }

                    if (type != null) {
                        newAstNode.props().put(PROP_TYPE, type.qualType);
                    } else if (node.hasNonNull("tagUsed")) {
                        newAstNode.props().put(PROP_TYPE, node.get("tagUsed").asText());
                    }

                    resultTree = newAstNode;
                }
    //                int nodeEndLine = endLineNode == null ? endLineNode2.asInt() : endLineNode.asInt();
    //                if (startLine <= curLine && endLine >= curLine) {
    //
    //                    spec.commandLine().getOut().println(node.has("type") ?
    //                            node.get("type").toPrettyString() : "UNK_TYPE");
    //                    spec.commandLine().getOut().println(node.get("range").toPrettyString());
    //                }
    //                JsonNode jsonNode = node.get("range").get("col").toString());
    //                if (jsonNode)
    //
            }
            JsonNode inner = node.get("inner");
            if (inner != null) {
                for (JsonNode child : inner) {
                    this.visit(child, resultTree);
                }
            }
        }
    }

    public static Ast runClang(String filePath) throws IOException {
        ClangParseConfig config = new ClangParseConfig(filePath);
        JsonNode root = dumpClangAstJson(config);
        JsonNode filteredRoot = filterClangAstByFile(root, config);
        return transformClangAstToUnifiedAst(filteredRoot, config);
    }

    public static JsonNode dumpClangAstJson(ClangParseConfig config) throws IOException {
        Process process = new ProcessBuilder().command(
                "clang",
                "-Xclang",
                "-ast-dump=json",
                "-fsyntax-only",
                config.filePath()).start();
        return new ObjectMapper().reader().readTree(process.getInputStream());
    }

    public static Ast transformClangAstToUnifiedAst(JsonNode root, ClangParseConfig config) {
        Map<String, String> watchedKinds = Map.of(
                "VarDecl", KIND_DEF_VAR,
                "FunctionDecl", KIND_DEF_FUNCTION,
                "ParmVarDecl", KIND_PARAM,
                "DeclRefExpr", KIND_REF,
                "TypedefDecl", KIND_DEF_TYPE,
                "RecordDecl", KIND_DEF_TYPE,
                "CXXRecordDecl", KIND_DEF_TYPE,
                "FieldDecl", KIND_DEF_FIELD
                );
        Ast astRoot = Ast.create("root", null, null);
        new Transformer(watchedKinds).visit(root, astRoot);
        return astRoot;
    }

    public static JsonNode filterClangAstByFile(JsonNode clangAst, ClangParseConfig config) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode filteredClangAst = mapper.createObjectNode();
        clangAst.fieldNames().forEachRemaining(field -> {
            if ("inner".equals(field)) {
                ArrayNode arrayNode = mapper.createArrayNode();
                Iterator<JsonNode> innerNodeIter = clangAst.get("inner").elements();
                while (innerNodeIter.hasNext()) {
                    JsonNode innerNode = innerNodeIter.next();
                    JsonNode locFileNode = innerNode.at("/loc/file");
                    if (!locFileNode.isMissingNode()) {
                        System.out.println(locFileNode.asText());
                        System.out.println(config.filePath());
                        System.out.println(Path.of(locFileNode.asText()));
                    }
                    if (!locFileNode.isMissingNode() && config.matchesFilePath(locFileNode.asText())) {
                        arrayNode.add(innerNode);
                        break;
                    }
                }
                while (innerNodeIter.hasNext()) {
                    arrayNode.add(innerNodeIter.next());
                }
                filteredClangAst.set(field, arrayNode);
            } else {
                filteredClangAst.set(field, clangAst.get(field));
            }
        });
        return filteredClangAst;
    }

    public record ClangType(String qualType, String desugaredQualType){
    }
    public record ClangRef(String id, String name, String kind, ClangType type) {
    }
    public record ClangAnnotation(String kind, SourceRange range, ClangType type, ClangRef ref) {
    }

    private static ClangType parseTypeNode(JsonNode typeNode) {
        if (null == typeNode || typeNode.isMissingNode() || typeNode.isNull()) {
            return null;
        }
        String qualType = typeNode.path("qualType").asText(null);
        String desugaredQualType = typeNode.path("desugaredQualType").asText(null);
        return new ClangType(qualType, desugaredQualType);
    }
}
