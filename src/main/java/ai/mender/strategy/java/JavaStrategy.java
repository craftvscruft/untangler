package ai.mender.strategy.java;

import ai.mender.domain.ReferencesResponse;
import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.strategy.ISourceFile;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.TopLevelNode;
import antlrgen.java20.Java20Lexer;
import antlrgen.java20.Java20Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

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
    public TopLevelNode parseTopLevel(ISourceFile sourceFile) {
        return new JavaTopLevelNode(parseProgram(sourceFile.getCharStream(), false));
    }

    @Override
    public ReferencesResponse references(TopLevelNode root, String name) {
        return ReferencesResponse.empty(name);
    }
}
