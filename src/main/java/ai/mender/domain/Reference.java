package ai.mender.domain;

import ai.mender.untangler.shared.response.SourceRange;

public record Reference(String name, SourceRange range, SourceRange declarationRange) {
}
