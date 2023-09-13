package ai.mender.domain;

import ai.mender.untangler.shared.SourceRange;

public record FunctionRec(String name, SourceRange range, int lineCount) {
    public FunctionRec(String name, SourceRange range) {
        this(name, range, range.end().line() - range.start().line() + 1);
    }
}
