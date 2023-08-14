package ai.mender.strategy.csharp;

import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import antlrgen.csharp.CSharpLexer;
import antlrgen.csharp.CSharpParser;
import org.antlr.v4.runtime.CommonTokenStream;



public class CSharpStrategy implements LanguageStrategy {

    private static CSharpParser.Compilation_unitContext parseTree(SourceFile sourceFile, boolean throwOnParseError) {
        CSharpLexer lexer = new CSharpLexer(sourceFile.getCharStream());
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

    @Override
    public TopLevelNode parseTopLevel(SourceFile sourceFile) {
        return new CSharpTopLevelNode(parseTree(sourceFile, false));
    }
}
