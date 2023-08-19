package ai.mender.strategy;

import ai.mender.SimpleSelector;
import ai.mender.domain.Reference;
import ai.mender.domain.ReferencesResponse;
import ai.mender.domain.SourceEditListResponse;
import ai.mender.domain.SourceRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;


public class TestLanguageStrategy {


    @Test
    public void createRenameEdits() {
        var declarations = new ArrayList<SourceRange>();
        var references = new ArrayList<Reference>();
        String declRange = "4:4-4:5";
        String ref1Range = "9:9-9:10";
        String ref2Range = "10:9-10:10";
        declarations.add(SourceRange.parse(declRange));

        references.add(new Reference("ds", SourceRange.parse(ref1Range), SourceRange.parse(declRange)));
        references.add(new Reference("ds", SourceRange.parse(ref2Range), SourceRange.parse(declRange)));
        ReferencesResponse referencesResponse = new ReferencesResponse(SimpleSelector.parse("to:1"), declarations, references);
        SourceRange declarationRange = SourceRange.parse(declRange);
        SourceEditListResponse editListResponse = LanguageStrategy.createRenameEdits("to", referencesResponse, declarationRange);

        Assertions.assertEquals(3, editListResponse.edits().size());
    }

    @Test
    public void createRenameEditsDedupeSameReferenceAppearsTwice() {
        var declarations = new ArrayList<SourceRange>();
        var references = new ArrayList<Reference>();
        String declRange = "4:4-4:5";
        String ref1Range = "9:9-9:10";
        declarations.add(SourceRange.parse(declRange));

        references.add(new Reference("ds", SourceRange.parse(ref1Range), SourceRange.parse(declRange)));
        references.add(new Reference("ds", SourceRange.parse(ref1Range), SourceRange.parse(declRange)));
        ReferencesResponse referencesResponse = new ReferencesResponse(SimpleSelector.parse("to:1"), declarations, references);
        SourceRange declarationRange = SourceRange.parse(declRange);
        SourceEditListResponse editListResponse = LanguageStrategy.createRenameEdits("to", referencesResponse, declarationRange);

        Assertions.assertEquals(2, editListResponse.edits().size());
    }
}
