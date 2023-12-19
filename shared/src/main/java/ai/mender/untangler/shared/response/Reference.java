package ai.mender.untangler.shared.response;

import ai.mender.untangler.shared.response.SourceRange;

import javax.swing.*;
import java.util.Comparator;

public record Reference(String name, SourceRange range, SourceRange declarationRange) implements Comparable<Reference> {
    @Override
    public int compareTo(Reference o) {
        return Comparator.comparing(Reference::name)
                .thenComparing(Reference::range)
                .thenComparing(Reference::declarationRange).compare(this, o);
    }
}
