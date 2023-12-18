package ai.mender.untangler.shared.response;

import ai.mender.untangler.shared.response.SourceRange;

public record Reference(String name, SourceRange range, SourceRange declarationRange) {
}
