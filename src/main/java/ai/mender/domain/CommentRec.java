package ai.mender.domain;

import ai.mender.untangler.shared.SourceRange;

public record CommentRec(SourceRange range, String text) {
}
