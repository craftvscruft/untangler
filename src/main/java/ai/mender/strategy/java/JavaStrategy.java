package ai.mender.strategy.java;

import ai.mender.SimpleSelector;
import ai.mender.untangler.shared.response.CommentRec;
import ai.mender.domain.ReferencesResponse;
import ai.mender.parsing.SyntaxTreeUtil;
import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.untangler.shared.ISourceFile;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.TopLevelNode;
import antlrgen.java20.Java20Lexer;
import antlrgen.java20.Java20Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Consumer;

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
        return new JavaTopLevelNode(parseProgram(SyntaxTreeUtil.fileToCharStream(sourceFile), false));
    }

    @Override
    public ReferencesResponse references(TopLevelNode root, SimpleSelector name) {
        throw new NotImplementedException("Reference operations not yet supported for this language");
    }

    @Override
    public void forEachComment(ISourceFile sourceFile, Consumer<CommentRec> consumer) {
        throw new NotImplementedException("Comment operations not yet supported for this language");
    }

    @Override
    public String formatMultiLineComment(String text) {
        return String.format("/* %s */\n", text);
    }
}
