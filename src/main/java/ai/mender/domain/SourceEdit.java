package ai.mender.domain;

public record SourceEdit(SourcePosition start, SourcePosition end, EditMode mode, String text) {
}
