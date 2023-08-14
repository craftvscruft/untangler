package ai.mender.strategy;

import ai.mender.Language;
import ai.mender.domain.FunctionRec;
import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.CppFunctionDefinitionNode;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.cpp14.CPP14ParserBaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CppStrategy implements LanguageStrategy {

    @Override
    public void collectFunctions(
            File file, List<FunctionRec> items, boolean throwOnParseError) {
        CPP14ParserBaseListener listener =
                new CPP14ParserBaseListener() {

                    @Override
                    public void enterFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
                        items.add(new CppFunctionDefinitionNode(ctx).toFunctionRec());
                    }
                };

        CPP14Parser.TranslationUnitContext tree = parseTree(file, throwOnParseError);
        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }

    private static CPP14Parser.TranslationUnitContext parseTree(File file, boolean throwOnParseError) {
        CharStream inputStream =
                null;
        try {
            inputStream = CharStreams.fromFileName(
                    file.getAbsolutePath(), CharsetUtils.detectFileCharset(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CPP14Parser.TranslationUnitContext tree =
                Language.parseProgram(inputStream, throwOnParseError);
        return tree;
    }
}
