package ai.mender.strategy.cpp;

import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import antlrgen.cpp14.CPP14Lexer;
import antlrgen.cpp14.CPP14Parser;
import org.antlr.v4.runtime.CommonTokenStream;

public class CppStrategy implements LanguageStrategy {

    @Override
    public TopLevelNode parseTopLevel(SourceFile sourceFile) {
        boolean throwOnParseError = false;
        CPP14Lexer lexer = new CPP14Lexer(sourceFile.getCharStream());
        if (throwOnParseError) {
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        }

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(commonTokenStream);
        if (throwOnParseError) {
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        }
        CPP14Parser.TranslationUnitContext tree =
                parser.translationUnit();
        return new CppTopLevelNode(tree);
    }
}
