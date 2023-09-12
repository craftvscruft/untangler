package ai.mender.commands;

import ai.mender.domain.SourcePosition;
import ai.mender.domain.SourceRange;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.File;


@CommandLine.Command(name = "expressions", mixinStandardHelpOptions = true,
        aliases = "expr",
        description = "Display expressions within a function")
public class GetExpressions implements Runnable, CommandLine.IExitCodeGenerator {

    @CommandLine.Parameters(index = "0", description = "selector (try a function name)", defaultValue = "*")
    private String selector;
    @CommandLine.Option(
            names = {"--file", "-f"},
            description = "The source code file to analyze",
            required = true,
            defaultValue = "${env:UNTANGLER_DEFAULT_FILE}")
    private File file;

    @CommandLine.Option(names = {"--output", "-o"}, defaultValue = "text",
            description = {
                    "Output format",
                    "Supported: text (default), json"
            })
    private OutputFormat outputFormat;

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;
    private boolean success = false;
    public record ClangType(String qualType, String desugaredQualType){

    }
    public record ClangRef(String name, String kind, ClangType type) {

    }
    public record ClangAnnotation(String kind, SourceRange range, ClangType type, ClangRef ref) {

    }
    void visit(JsonNode node, int curLine) {
//        if (node.has("name")) {
//            spec.commandLine().getOut().println("name: " + node.get("name"));
//        }
//        spec.commandLine().getOut().println("id: " + node.get("id"));
//        spec.commandLine().getOut().println("kind: " + node.get("kind"));

        JsonNode locLineNode = node.at("/loc/line");
        if (!locLineNode.isMissingNode()) {
            curLine = locLineNode.asInt();
        }
        // Kind
//        type
//        "desugaredQualType" : "int (int, char **)",
//                "qualType" : "int (int, char **)"
//        referencedDecl
//        "kind" : "FunctionDecl",
//                "name" : "clock",
//                "type" : {
//            "qualType" : "clock_t (void)"
//        }
        if (node.has("range")) {
//                spec.commandLine().getOut().println("kind: " + node.get("range").toPrettyString());
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
                SourceRange range = new SourceRange(new SourcePosition(curLine, beginCol), new SourcePosition(endLine, endCol));
                String kind = node.path("kind").asText("UNKNOWN");
                JsonNode typeNode = node.get("type");
                ClangType type = parseTypeNode(typeNode);
                ClangRef ref = null;
                if (node.has("referencedDecl")) {
                    JsonNode refNode = node.get("referencedDecl");
                    String refName = refNode.path("name").asText("UNKNOWN");
                    String refKind = refNode.path("kind").asText("UNKNOWN");
                    ref = new ClangRef(refName, refKind, parseTypeNode(refNode.get("type")));
                }
                System.out.println(new ClangAnnotation(kind, range, type, ref));
//                System.out.println(String.format("%s:%s-%s:%s", curLine, beginCol, endLine, endCol));
//                if (node.has("type")) {
//                    System.out.println(typeNode.toPrettyString());
//                }

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
//        spec.commandLine().getOut().println("line: " + curLine);
//            }

        JsonNode inner = node.get("inner");
        if (inner != null) {
            for (JsonNode child : inner) {
                visit(child, curLine);
            }
        }
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

    ;
    @Override
    public void run() {
        var message = "OK";
        try {
            SourceFile sourceFile = new SourceFile(file);
            LanguageStrategy languageStrategy = sourceFile.createStrategyForFile();
            if (!file.exists()) {
                message = "File not found";
                success = false;
            } else if (languageStrategy == null) {
                message = "Unknown file type! Cannot parse.";
                success = false;
            } else {
//                languageStrategy.forEachComment(sourceFile, comment -> {
//                    if (Console.isLineMatch(comment.range(), line)) {
//                        items.add(comment);
//                    }
//                });
                String filePath = file.getAbsolutePath();
                Process process = new ProcessBuilder().command(
                        "clang",
                        "-Xclang",
                        "-ast-dump=json",
                        "-Xclang",
                        "-ast-dump-filter=" + selector,
                        "-fsyntax-only",
                        filePath).start();
//                process.waitFor();
//                String result = new String(process.getInputStream().readAllBytes());
//                spec.commandLine().getOut().println(result);
                JsonNode root = new ObjectMapper().reader().readTree(process.getInputStream());
                visit(root, 54);

                success = true;
            }
        } catch (Exception e) {
            message = e.getMessage();

        }
        spec.commandLine().getOut().println(message);
//        Console.printOutput("", spec.commandLine().getOut(), outputFormat);
    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
