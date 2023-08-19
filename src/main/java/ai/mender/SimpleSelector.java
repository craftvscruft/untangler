package ai.mender;

import ai.mender.domain.SourceRange;
import org.apache.commons.lang3.StringUtils;

public record SimpleSelector(String name, int line) {
    public static final int ANY_LINE = -1;
    public static final String ANY_NAME = "*";

    public static SimpleSelector parse(String input) {
        if (StringUtils.isBlank(input)) {
            return new SimpleSelector(ANY_NAME, ANY_LINE);
        }
        String[] split = input.split(":");
        if (split.length < 2 || !StringUtils.isNumeric(split[1])) {
            String name = split[0];
            return new SimpleSelector(name, ANY_LINE);
        }
        String name = split[0];
        int line = Integer.parseInt(split[1]);
        return new SimpleSelector(name, line);
    }

    public boolean hasLine() {
        return line != ANY_LINE;
    }

    public boolean matchesName(String name) {
        return ANY_NAME.equals(this.name) || this.name.equals(name);
    }

    public boolean matchesLine(SourceRange range) {
        return line == ANY_LINE || (range.start().line() <= this.line && range.end().line() >= this.line);
    }
}
