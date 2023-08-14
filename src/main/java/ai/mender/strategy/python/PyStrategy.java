package ai.mender.strategy.python;

import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import antlrgen.python.PythonLexer;
import antlrgen.python.PythonParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

public class PyStrategy implements LanguageStrategy {

    @Override
    public TopLevelNode parseTopLevel(SourceFile sourceFile) {
        return new PythonTopLevelNode(parseTree(sourceFile));
    }

    private static PythonParser.File_inputContext parseTree(SourceFile sourceFile) {
        boolean throwOnParseError = false;
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(
                    sourceFile.file().getAbsolutePath(), CharsetUtils.detectFileCharset(sourceFile.file()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PythonLexer lexer = new PythonLexer(inputStream);
        if (throwOnParseError) {
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        }

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(commonTokenStream);
        if (throwOnParseError) {
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        }
        PythonParser.File_inputContext tree = parser.file_input();
        return tree;
    }
}
