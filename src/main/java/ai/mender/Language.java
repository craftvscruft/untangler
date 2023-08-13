package ai.mender;

import ai.mender.parsing.ThrowingErrorListener;
import antlrgen.CPP14Lexer;
import antlrgen.CPP14Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

public class Language {

    public static String getTextIncludingWhitespace(ParserRuleContext ctx) {
        if (null == ctx) {
            return "";
        }
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        return ctx.start.getInputStream().getText(new Interval(a, b));
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
}
