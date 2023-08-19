package ai.mender.domain;

public record SourceRange(SourcePosition start, SourcePosition end) {
    public static SourceRange unknown() {
        return new SourceRange(new SourcePosition(-1, -1), new SourcePosition(-1, -1));
    }

    public static SourceRange parse(String text) {
        // Example: 3:49-3:50
        String[] startEnd = text.split("-");
        String startText = startEnd[0];
        String endText = startEnd[1];
        return new SourceRange(SourcePosition.parse(startText), SourcePosition.parse(endText));
    }

    @Override
    public String toString() {
        return String.format("[%s:%s, %s:%s]", start.line(), start.col(), end.line(), end.col());
    }

    public boolean isUnknown() {
        return start.line() == -1;
    }

    public boolean isWithin(SourceRange outerRange) {
        return start.isWithin(outerRange) && end.isWithin(outerRange);
    }
}
