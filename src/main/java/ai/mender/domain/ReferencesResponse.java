package ai.mender.domain;

import java.util.ArrayList;
import java.util.List;

public record ReferencesResponse(String name, List<SourceRange> declarations, List<Reference> references) {
    public static ReferencesResponse empty(String name) {
        return new ReferencesResponse(name, new ArrayList<>(), new ArrayList<>());
    }
}
