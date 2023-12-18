package ai.mender.domain;

import ai.mender.untangler.shared.response.SourceRange;

public record CommentRec(SourceRange range, String text) {
}
