package ai.mender.parsing;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SyntaxTreeUtil {
    private SyntaxTreeUtil() {

    }
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


}