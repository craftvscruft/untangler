package ai.mender.commands;

import ai.mender.Console;
import ai.mender.Language;
import ai.mender.domain.FunctionRec;
import ai.mender.domain.ListResponse;
import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.CppFunctionDefinitionNode;
import antlrgen.CPP14Parser;
import antlrgen.CPP14ParserBaseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import picocli.CommandLine;

@CommandLine.Command(
        name = "functions",
        mixinStandardHelpOptions = true,
        description = "Lists functions defined in a file as json",
        aliases = {"function", "fns", "fn", "fun"})
public class GetFunctionsCmd implements Runnable, CommandLine.IExitCodeGenerator {
    @CommandLine.Parameters(index = "0", description = "The source code file to analyze")
    private File file;

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;
    private boolean success = false;

    @Override
    public void run() {
        var items = new ArrayList<FunctionRec>();
        var message = "OK";
        String extension = getExtension(file);
        try {

            switch (extension.toLowerCase()) {
                case "c", "cpp", "cc" -> {
                    collectCppFunctions(file, items, false);
                    success = true;
                }
                default -> {
                    message = "Unknown file type! Cannot parse.";
                }
            }
        } catch (IOException e) {
            message = e.getMessage();
            success = true;
        }
        ListResponse<FunctionRec> response = new ListResponse<>(success, message, items);
        Console.printJson(response, spec.commandLine().getOut());
    }

    private static void collectCppFunctions(
            File file, List<FunctionRec> items, boolean throwOnParseError) throws IOException {
        CPP14ParserBaseListener listener =
                new CPP14ParserBaseListener() {

                    @Override
                    public void enterFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
                        items.add(new CppFunctionDefinitionNode(ctx).toFunctionRec());
                    }
                };
        CharStream inputStream =
                CharStreams.fromFileName(
                        file.getAbsolutePath(), CharsetUtils.detectFileCharset(file));
        CPP14Parser.TranslationUnitContext tree =
                Language.parseProgram(inputStream, throwOnParseError);
        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }

    private String getExtension(File file) {
        var extension = "";
        String[] split = file.getName().split("\\.");
        if (split.length > 1) {
            extension = split[split.length - 1];
        }
        return extension;
    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
