package ai.mender.strategy;

import ai.mender.domain.FunctionRec;
import ai.mender.parsing.CSharpFunctionDefinitionNode;
import ai.mender.parsing.CharsetUtils;
import ai.mender.parsing.ThrowingErrorListener;
import antlrgen.csharp.CSharpLexer;
import antlrgen.csharp.CSharpParser;
import antlrgen.csharp.CSharpParserBaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CSharpStrategy implements LanguageStrategy {
    @Override
    public void collectFunctions(File file, List<FunctionRec> items, boolean throwOnParseError) {
        CSharpParserBaseListener listener =
                new CSharpParserBaseListener() {

                    @Override
                    public void enterMethod_declaration(CSharpParser.Method_declarationContext ctx) {
                        items.add(new CSharpFunctionDefinitionNode(ctx).toFunctionRec());
                    }
                };

        var tree = parseTree(file, throwOnParseError);
        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }

    private static CSharpParser.Compilation_unitContext parseTree(File file, boolean throwOnParseError) {
        CharStream inputStream = getCharStream(file);
        CSharpLexer lexer = new CSharpLexer(inputStream);
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

    private static CharStream getCharStream(File file) {
        CharStream inputStream;
        try {
            inputStream = CharStreams.fromFileName(
                    file.getAbsolutePath(), CharsetUtils.detectFileCharset(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return inputStream;
    }

}
