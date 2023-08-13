package ai.mender;

import antlrgen.CPP14Lexer;
import antlrgen.CPP14Parser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

public class Language {
    public static <T> String toJson(T object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper() {};

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper.setDefaultPrettyPrinter(new JsonPrettyPrinter()).writeValueAsString(object);
    }

    public static String getTextIncludingWhitespace(ParserRuleContext ctx) {
        if (null == ctx) {
            return "";
        }
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        return ctx.start.getInputStream().getText(new Interval(a, b));
    }

    public static CPP14Parser.TranslationUnitContext parseProgram(CharStream inputStream) {
        CPP14Lexer lexer = new CPP14Lexer(inputStream);
//        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(commonTokenStream);
//        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        return parser.translationUnit();
    }
}
