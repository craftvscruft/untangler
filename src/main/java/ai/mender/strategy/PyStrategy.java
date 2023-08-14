package ai.mender.strategy;

import ai.mender.Language;
import ai.mender.domain.FunctionRec;
import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.CppFunctionDefinitionNode;
import ai.mender.parsing.PythonFunctionDefinitionNode;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.cpp14.CPP14ParserBaseListener;
import antlrgen.python.PythonParser;
import antlrgen.python.PythonParserBaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
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
        PythonParser.File_inputContext tree = Language.parsePyProgram(inputStream, throwOnParseError);
        return tree;
    }
}
