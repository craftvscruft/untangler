package ai.mender.domain;

public record Reference(String name, SourceRange range, SourceRange declarationRange) {
}
