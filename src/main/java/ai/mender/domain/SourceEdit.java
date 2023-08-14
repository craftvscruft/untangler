package ai.mender.domain;

public record SourceEdit(SourcePosition start, SourcePosition end, String text, EditMode mode) {
}
