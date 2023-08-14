package ai.mender.strategy.csharp;

import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import antlrgen.csharp.CSharpLexer;
import antlrgen.csharp.CSharpParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.IOException;


public class CSharpStrategy implements LanguageStrategy {
//    @Override
//    public void collectFunctions(File file, List<FunctionRec> items, boolean throwOnParseError) {
//
//    }

    private static CSharpParser.Compilation_unitContext parseTree(File file, boolean throwOnParseError) {
        CharStream inputStream = getCharStream(file);
        CSharpLexer lexer = new CSharpLexer(inputStream);
        if (throwOnParseError) {
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        }

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        CSharpParser parser = new CSharpParser(commonTokenStream);
        if (throwOnParseError) {
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        }

        return parser.compilation_unit();
    }

    private static CharStream getCharStream(File file) {
        CharStream inputStream;
        try {
            inputStream = CharStreams.fromFileName(
                    file.getAbsolutePath(), CharsetUtils.detectFileCharset(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    @Override
    public TopLevelNode parseTopLevel(SourceFile sourceFile) {
        return new CSharpTopLevelNode(parseTree(sourceFile.file(), false));
    }
}
