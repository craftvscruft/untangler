package ai.mender.domain;

import org.apache.commons.lang3.compare.ComparableUtils;

import java.util.Comparator;

public record SourcePosition(int line, int col) implements Comparable<SourcePosition> {

    @Override
    public String toString() {
        return String.format("%s:%s", line, col);
    }



    @Override
    public int compareTo(SourcePosition o) {
        return Comparator.comparing(SourcePosition::line).thenComparing(SourcePosition::col).compare(this, o);
    }

    public boolean isWithin(SourceRange outerRange) {
        // Inclusive
        return ComparableUtils.is(this).between(outerRange.start(), outerRange.end());
    }
}
