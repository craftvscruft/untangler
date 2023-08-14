package ai.mender.strategy;

import ai.mender.domain.FunctionRec;
import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.PythonFunctionDefinitionNode;
import ai.mender.parsing.ThrowingErrorListener;
import antlrgen.python.PythonLexer;
import antlrgen.python.PythonParser;
import antlrgen.python.PythonParserBaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PyStrategy implements LanguageStrategy {

    @Override
    public void collectFunctions(
            File file, List<FunctionRec> items, boolean throwOnParseError) {
        PythonParserBaseListener listener =
                new PythonParserBaseListener() {

                    @Override
                    public void enterFuncdef(PythonParser.FuncdefContext ctx) {
                        items.add(new PythonFunctionDefinitionNode(ctx).toFunctionRec());
                    }
                };

        PythonParser.File_inputContext tree = parseTree(file, throwOnParseError);
        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }

    private static PythonParser.File_inputContext parseTree(File file, boolean throwOnParseError) {
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(
                    file.getAbsolutePath(), CharsetUtils.detectFileCharset(file));
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
