package ai.mender.untangler.clang;

import ai.mender.untangler.shared.Ast;
import ai.mender.untangler.shared.SourcePosition;
import ai.mender.untangler.shared.SourceRange;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class ClangRunner {
    static class Transformer {

        private final Path filePath;
        private final Map<String, String> watchedKinds;
        private int curLine;
        private boolean inFile;
        private boolean debug = false;


        public Transformer(Path filePath, Map<String, String> watchedKinds) {
            this.filePath = filePath;
            this.watchedKinds = watchedKinds;
            this.curLine = -1;
            this.inFile = false;
        }

        public void visit(JsonNode node, Ast resultTree) {
            if (!inFile) {
                JsonNode locFileNode = node.at("/loc/file");
                if (!locFileNode.isMissingNode() && filePath.equals(Path.of(locFileNode.asText()))) {
                    inFile = true;
                }
            }
            JsonNode locLineNode = node.at("/loc/line");
            if (!locLineNode.isMissingNode()) {
                curLine = locLineNode.asInt();
            }
            if (inFile && node.has("range")) {
                JsonNode beginLineNode = node.at("/range/begin/line");
                JsonNode beginLineNode2 = node.at("/range/begin/expansionLoc/line");
                JsonNode beginColNode = node.at("/range/begin/col");
                JsonNode beginColNode2 = node.at("/range/begin/expansionLoc/col");
    
                int beginLineBackup = beginLineNode2.isMissingNode() ? curLine : beginLineNode2.asInt();
                curLine = beginLineNode.isMissingNode() ? beginLineBackup : beginLineNode.asInt();
    
                int beginColBackup = beginColNode2.isMissingNode() ? -1 : beginColNode2.asInt();
                int beginCol = beginColNode.isMissingNode() ? beginColBackup : beginColNode.asInt();
    
                JsonNode endLineNode = node.at("/range/end/line");
                JsonNode endLineNode2 = node.at("/range/end/expansionLoc/line");
                JsonNode endColNode = node.at("/range/end/col");
                JsonNode endColNode2 = node.at("/range/end/expansionLoc/col");
    
                int endLineBackup = endLineNode2.isMissingNode() ? curLine : endLineNode2.asInt();
                int endLine = endLineNode.isMissingNode() ? endLineBackup : endLineNode.asInt();
    
                int endColBackup = endColNode2.isMissingNode() ? -1 : endColNode2.asInt();
                int endCol = endColNode.isMissingNode() ? endColBackup : endColNode.asInt();
                if (node.has("type")) {
                    String id = node.path("id").asText(null);
                    String name = node.path("name").asText(null);
                    SourceRange range = new SourceRange(new SourcePosition(curLine, beginCol), new SourcePosition(endLine, endCol));
                    String kind = node.path("kind").asText("UNKNOWN");
                    JsonNode typeNode = node.get("type");
                    ClangType type = parseTypeNode(typeNode);
                    ClangRef ref = null;
                    if (node.has("referencedDecl")) {
                        JsonNode refNode = node.get("referencedDecl");
                        String refName = refNode.path("name").asText("UNKNOWN");
                        String refKind = refNode.path("kind").asText("UNKNOWN");
                        String refId = refNode.path("id").asText("UNKNOWN");
                        ref = new ClangRef(refId, refName, refKind, parseTypeNode(refNode.get("type")));
                    }
                    if (watchedKinds.containsKey(kind)) {
                        if (debug) {
                            System.out.println(new ClangAnnotation(kind, range, type, ref));
                        }
                        Ast newAstNode = Ast.create(watchedKinds.get(kind), range);
                        resultTree.children().add(newAstNode);
                        if (name != null) {
                            newAstNode.props().put("name", name);
                        }
                        if (id != null) {
                            newAstNode.props().put("id", id);
                        }
                        if (ref != null) {
                            newAstNode.props().put("refId", ref.id);
                            if (name == null) {
                                newAstNode.props().put("name", ref.name);
                            }
                        }

                        if (type != null) {
                            newAstNode.props().put("type", type.qualType);
                        }

                        resultTree = newAstNode;

                    }
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

    public static void runClang(String filePath) throws IOException {
        Map<String, String> watchedKinds = Map.of(
                "VarDecl", "def:var",
                "FunctionDecl", "def:fn",
                "ParmVarDecl", "param",
                "DeclRefExpr", "ref",
                "TypedefDecl", "def:type");
        Process process = new ProcessBuilder().command(
                "clang",
                "-Xclang",
                "-ast-dump=json",
                "-fsyntax-only",
                filePath).start();
//                process.waitFor();
//                String result = new String(process.getInputStream().readAllBytes());
//                spec.commandLine().getOut().println(result);
        JsonNode root = new ObjectMapper().reader().readTree(process.getInputStream());
        Ast astRoot = Ast.create("root", null);
        new Transformer(Path.of(filePath), watchedKinds).visit(root, astRoot);
        SimpleModule module = new SimpleModule();
        module.addSerializer(new StdSerializer<>(SourceRange.class) {

            @Override
            public void serialize(SourceRange value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString(value.toString());
            }
        });
        ObjectWriter writer = new ObjectMapper().registerModule(module).writer();
        writer.withDefaultPrettyPrinter().writeValue(System.out, astRoot);
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
        ClangType type = new ClangType(qualType, desugaredQualType);
        return type;
    }
}
