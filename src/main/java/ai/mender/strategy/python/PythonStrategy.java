package ai.mender.strategy.python;

import ai.mender.SimpleSelector;
import ai.mender.domain.CommentRec;
import ai.mender.domain.ReferencesResponse;
import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.strategy.ISourceFile;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.TopLevelNode;
import antlrgen.python.PythonLexer;
import antlrgen.python.PythonParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Consumer;

public class PythonStrategy implements LanguageStrategy {

    @Override
    public TopLevelNode parseTopLevel(ISourceFile sourceFile) {
        return new PythonTopLevelNode(parseTree(sourceFile));
    }

    private static PythonParser.File_inputContext parseTree(ISourceFile sourceFile) {
        boolean throwOnParseError = false;
        PythonLexer lexer = new PythonLexer(sourceFile.getCharStream());
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
    @Override
    public ReferencesResponse references(TopLevelNode root, SimpleSelector name) {
        throw new NotImplementedException("Reference operations not yet supported for this language");
    }

    @Override
    public void forEachComment(ISourceFile sourceFile, Consumer<CommentRec> consumer) {
        throw new NotImplementedException("Comment operations not yet supported for this language");
    }
}
