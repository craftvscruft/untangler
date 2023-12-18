package ai.mender.untangler.shared.response;
import ai.mender.untangler.shared.response.SourceRange;
import ai.mender.untangler.shared.response.SourceText;

import java.util.List;

public record DeclarationRec(SourceRange range, String specifier, List<SourceText> declarators) {
}
