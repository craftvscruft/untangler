package ai.mender.domain;

import ai.mender.SimpleSelector;

import java.util.ArrayList;
import java.util.List;

public record ReferencesResponse(SimpleSelector selector, List<SourceRange> declarations, List<Reference> references) {
    public static ReferencesResponse empty(SimpleSelector selector) {
        return new ReferencesResponse(selector, new ArrayList<>(), new ArrayList<>());
    }
}
