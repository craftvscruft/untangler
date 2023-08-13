package ai.mender.commands;

import ai.mender.Console;
import ai.mender.Language;
import ai.mender.domain.FunctionRec;
import ai.mender.domain.ListResponse;
import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.CppFunctionDefinitionNode;
import antlrgen.CPP14Parser;
import antlrgen.CPP14ParserBaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


@CommandLine.Command(name = "functions", mixinStandardHelpOptions = true,
        description = "Lists functions defined in a file as json", aliases = {"function", "fns", "fn", "fun"})
public class GetFunctionsCmd implements Runnable {
    @CommandLine.Parameters(index = "0", description = "The source code file to analyze")
    private File file;


    @Override
    public void run() {
        var items = new ArrayList<FunctionRec>();
        var message = "OK";
        var success = true;
        CPP14ParserBaseListener listener = new CPP14ParserBaseListener() {

            @Override
            public void enterFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
                items.add(new CppFunctionDefinitionNode(ctx).toFunctionRec());
            }
        };

        try {
            String extension = getExtension(file);
            if (extension.equalsIgnoreCase("c")) {
                CharStream inputStream = CharStreams.fromFileName(file.getAbsolutePath(), CharsetUtils.detectFileCharset(file));
                CPP14Parser.TranslationUnitContext tree = Language.parseProgram(inputStream);
                ParseTreeWalker.DEFAULT.walk(listener, tree);
            } else {
                message = "Unknown file type! Cannot parse.";
                success = false;
            }

            Console.printJson(new ListResponse<>(success, message, items));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getExtension(File file) {
        var extension = "";
        String[] split = file.getName().split("\\.");
        if (split.length > 1) {
            extension = split[split.length - 1];
        }
        return extension;
    }

}
