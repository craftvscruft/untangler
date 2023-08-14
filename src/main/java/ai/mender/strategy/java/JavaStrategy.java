package ai.mender.strategy.java;

import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import antlrgen.java20.Java20Lexer;
import antlrgen.java20.Java20Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

public class JavaStrategy implements LanguageStrategy {

    private static Java20Parser.CompilationUnitContext parseProgram(
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

    @Override
    public TopLevelNode parseTopLevel(SourceFile sourceFile) {
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(
                    sourceFile.file().getAbsolutePath(), CharsetUtils.detectFileCharset(sourceFile.file()));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return new JavaTopLevelNode(parseProgram(inputStream, false));
    }
}
