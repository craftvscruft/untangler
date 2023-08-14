package ai.mender;

import ai.mender.parsing.ThrowingErrorListener;
import antlrgen.cpp14.CPP14Lexer;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.python.PythonLexer;
import antlrgen.python.PythonParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.function.Function;

public class Language {

    public static String getTextIncludingWhitespace(ParserRuleContext ctx) {
        if (null == ctx) {
            return "";
        }
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        return ctx.start.getInputStream().getText(new Interval(a, b));
    }

    public static String getTextIncludingWhitespace(TerminalNode ctx) {
        if (null == ctx) {
            return "";
        }
        return ctx.getText();
    }

    public static CPP14Parser.TranslationUnitContext parseProgram(
            CharStream inputStream, boolean throwOnParseError) {
        CPP14Lexer lexer = new CPP14Lexer(inputStream);
        if (throwOnParseError) {
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        }

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(commonTokenStream);
        if (throwOnParseError) {
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        }
        return parser.translationUnit();
    }

    public static PythonParser.File_inputContext parsePyProgram(
            CharStream inputStream, boolean throwOnParseError) {
        PythonLexer lexer = new PythonLexer(inputStream);
        if (throwOnParseError) {
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        }

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(commonTokenStream);
        if (throwOnParseError) {
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        }
        return parser.file_input();
    }


}
