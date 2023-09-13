package ai.mender.domain;
import ai.mender.untangler.shared.SourceRange;
import ai.mender.untangler.shared.SourceText;

import java.util.List;

public record DeclarationRec(SourceRange range, String specifier, List<SourceText> declarators) {
}
