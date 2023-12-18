package ai.mender.domain;

import ai.mender.untangler.shared.response.SourcePosition;
import ai.mender.untangler.shared.response.SourceRange;

public record SourceEdit(SourcePosition start, SourcePosition end, EditMode mode, String text) {
    public static SourceEdit insert(SourcePosition position, String text) {
        return new SourceEdit(position, position, EditMode.Insert, text);
    }

    public static SourceEdit replace(SourceRange range, String text) {
        return new SourceEdit(range.start(), range.end(), EditMode.Insert, text);
    }
}
