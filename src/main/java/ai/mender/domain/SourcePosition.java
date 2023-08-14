package ai.mender.domain;

public record SourcePosition(int line, int col) {

    @Override
    public String toString() {
        return String.format("%s:%s", line, col);
    }
}
