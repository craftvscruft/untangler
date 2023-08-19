package ai.mender.domain;
import java.util.List;

public record DeclarationRec(SourceRange range, String specifier, List<SourceText> declarators) {
}
