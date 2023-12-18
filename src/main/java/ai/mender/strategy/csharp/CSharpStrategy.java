package ai.mender.strategy.csharp;

import ai.mender.untangler.shared.SimpleSelector;
import ai.mender.untangler.shared.response.CommentRec;
import ai.mender.untangler.shared.response.ReferencesResponse;
import ai.mender.parsing.SyntaxTreeUtil;
import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.untangler.shared.ISourceFile;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.TopLevelNode;
import antlrgen.csharp.CSharpLexer;
import antlrgen.csharp.CSharpParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Consumer;


public class CSharpStrategy implements LanguageStrategy {

    private static CSharpParser.Compilation_unitContext parseTree(ISourceFile sourceFile, boolean throwOnParseError) {
        CSharpLexer lexer = new CSharpLexer(SyntaxTreeUtil.fileToCharStream(sourceFile));
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
    public TopLevelNode parseTopLevel(ISourceFile sourceFile) {
        return new CSharpTopLevelNode(parseTree(sourceFile, false));
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
