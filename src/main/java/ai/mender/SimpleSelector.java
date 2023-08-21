package ai.mender;

import ai.mender.domain.SourceRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public record SimpleSelector(String name, int startLine, int endLine) {
    public static final int ANY_LINE = -1;
    public static final String ANY_NAME = "*";

    public static SimpleSelector parse(String input) {
        if (StringUtils.isBlank(input)) {
            return new SimpleSelector(ANY_NAME, ANY_LINE, ANY_LINE);
        }
        String[] split = input.split(":");
        String name = split[0];
        if (split.length < 2) {

            return new SimpleSelector(name, ANY_LINE, ANY_LINE);
        }
//
        var linePair = parseLineSelector(split[1]);
        return new SimpleSelector(name, linePair.getLeft(), linePair.getRight());
    }

    private static Pair<Integer, Integer> parseLineSelector(String text) {
        String[] split = text.split("-");
        if (!StringUtils.isNumeric(split[0])) {
            return Pair.of(ANY_LINE, ANY_LINE);
        }
        int startLine = Integer.parseInt(split[0]);
        if (split.length < 2 || !StringUtils.isNumeric(split[1])) {
            return Pair.of(startLine, startLine);
        }
        return Pair.of(startLine, Integer.parseInt(split[1]));
    }

    public boolean hasLine() {
        return startLine != ANY_LINE;
    }

    public boolean matchesName(String name) {
        return ANY_NAME.equals(this.name) || this.name.equals(name);
    }

    public boolean matchesLineRange(SourceRange range) {
        return startLine == ANY_LINE || matchesLine(range.start().line()) || matchesLine(range.end().line());
    }

    private boolean matchesLine(int line) {
        return line >= this.startLine && line <= this.endLine;
    }
}
