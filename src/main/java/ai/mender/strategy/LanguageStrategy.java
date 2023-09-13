package ai.mender.strategy;

import ai.mender.SimpleSelector;
import ai.mender.domain.*;
import ai.mender.untangler.shared.SourcePosition;
import ai.mender.untangler.shared.SourceRange;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface LanguageStrategy {
    TopLevelNode parseTopLevel(ISourceFile sourceFile);

    default SourceEditListResponse rename(TopLevelNode root, SimpleSelector fromSelector, String to) {
        var referencesResponse = references(root, fromSelector);
        List<SourceRange> selectedDeclarations = referencesResponse.declarations();
        if (fromSelector.hasLine()) {
            List<SourceRange> declarations = referencesResponse.declarations();

            Stream<SourceRange> matchedDeclarations = declarations.stream()
                    .filter(sourceRange -> fromSelector.matchesLineRange(sourceRange));
            Stream<SourceRange> matchedReferenceDeclarations = referencesResponse.references().stream()
                    .filter(ref -> fromSelector.matchesLineRange(ref.range()))
                    .map(Reference::declarationRange);
            selectedDeclarations = Stream.concat(matchedDeclarations, matchedReferenceDeclarations)
                    .distinct()
                    .toList();
        }

        if (selectedDeclarations.isEmpty()) {
            return new SourceEditListResponse(false, "No matching declarations found in file", Arrays.asList());
        }
        if (selectedDeclarations.size() > 1) {
            String options = selectedDeclarations.stream().map(decl -> String.format("%s:%s", fromSelector.name(), decl.start().line())).collect(Collectors.joining("\n"));
            String message = "Multiple matches, please specify with <name>:<line>, one of:\n" + options;
            return new SourceEditListResponse(false, message, Arrays.asList());
        }
        SourceRange declarationRange = selectedDeclarations.get(0);
        return createRenameEdits(to, referencesResponse, declarationRange);
    }

    static SourceEditListResponse createRenameEdits(String to, ReferencesResponse referencesResponse, SourceRange declarationRange) {
        SourceEdit declarationEdit = new SourceEdit(declarationRange.start(), declarationRange.end(), EditMode.Replace, to);
        Stream<SourceEdit> referenceEdits = referencesResponse.references().stream()
                .filter(ref -> declarationRange.equals(ref.declarationRange()))
                .map(ref -> new SourceEdit(ref.range().start(), ref.range().end(), EditMode.Replace, to));
        List<SourceEdit> edits = Stream.concat(List.of(declarationEdit).stream(), referenceEdits)
                .distinct()
                .toList();
        return new SourceEditListResponse(true, "OK", edits);
    }

    ReferencesResponse references(TopLevelNode root, SimpleSelector selector);

    void forEachComment(ISourceFile sourceFile, Consumer<CommentRec> consumer);

    default SourceEditListResponse insertComment(TopLevelNode tree, int line, String text) {
        String commentText = formatMultiLineComment(text.trim());
        SourceEdit edit = SourceEdit.insert(new SourcePosition(line, 1), commentText);
        return new SourceEditListResponse(true, "OK", List.of(edit));
    }

    String formatMultiLineComment(String text);


}
