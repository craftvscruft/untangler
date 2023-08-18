package ai.mender.strategy.python;

import ai.mender.domain.ReferencesResponse;
import ai.mender.parsing.ThrowingErrorListener;
import ai.mender.strategy.ISourceFile;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.TopLevelNode;
import antlrgen.python.PythonLexer;
import antlrgen.python.PythonParser;
import org.antlr.v4.runtime.CommonTokenStream;

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
    public ReferencesResponse references(TopLevelNode root, String name) {
        return ReferencesResponse.empty(name);
    }
}
