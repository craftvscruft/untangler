package ai.mender.domain;

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
}
