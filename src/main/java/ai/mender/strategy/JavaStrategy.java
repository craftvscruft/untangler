package ai.mender.strategy;

import ai.mender.domain.FunctionRec;
import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.JavaFunctionDefinitionNode;
import ai.mender.parsing.ThrowingErrorListener;
import antlrgen.java20.Java20Lexer;
import antlrgen.java20.Java20Parser;
import antlrgen.java20.Java20ParserBaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JavaStrategy implements LanguageStrategy {
    @Override
    public void collectFunctions(
            File file, List<FunctionRec> items, boolean throwOnParseError) {
        Java20ParserBaseListener listener =
                new Java20ParserBaseListener() {

                    @Override
                    public void enterMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
                        items.add(new JavaFunctionDefinitionNode(ctx).toFunctionRec());
                    }
                };

        var tree = parseTree(file, throwOnParseError);
        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }

    private static Java20Parser.CompilationUnitContext parseTree(File file, boolean throwOnParseError) {
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(
                    file.getAbsolutePath(), CharsetUtils.detectFileCharset(file));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return parseProgram(inputStream, throwOnParseError);
    }

    public static Java20Parser.CompilationUnitContext parseProgram(
            CharStream inputStream, boolean throwOnParseError) {
        Java20Lexer lexer = new Java20Lexer(inputStream);
        if (throwOnParseError) {
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        }

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        Java20Parser parser = new Java20Parser(commonTokenStream);
        if (throwOnParseError) {
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        }
        return parser.compilationUnit();
    }
}
