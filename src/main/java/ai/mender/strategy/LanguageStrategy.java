package ai.mender.strategy;

import ai.mender.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface LanguageStrategy {
    TopLevelNode parseTopLevel(ISourceFile sourceFile);

    default SourceEditListResponse rename(TopLevelNode root, String fromSelector, String to) {
        String[] fromParts = fromSelector.split(":");
        String from = fromParts[0];

        var referencesResponse = references(root, from);
        List<SourceRange> declarations = referencesResponse.declarations();
        if (fromParts.length > 1) {
            // check format and throw something friendly here?
            int lineSelector = Integer.parseInt(fromParts[1]);
            declarations = declarations.stream().filter(sourceRange -> sourceRange.start().line() == lineSelector
            ).toList();
        }
        if (declarations.isEmpty()) {
            return new SourceEditListResponse(false, "No matching declarations found in file", Arrays.asList());
        }
        if (declarations.size() > 1) {
            String options = declarations.stream().map(decl -> String.format("%s:%s", from, decl.start().line())).collect(Collectors.joining("\n"));
            String message = "Multiple matches, please specify with <name>:<line>, one of:\n" + options;
            return new SourceEditListResponse(false, message, Arrays.asList());
        }
        SourceRange declarationRange = declarations.get(0);
        var results = new ArrayList<SourceEdit>();
        results.add(new SourceEdit(declarationRange.start(), declarationRange.end(), to, EditMode.Replace));
        referencesResponse.references().stream()
                .filter(ref -> declarationRange.equals(ref.declarationRange()))
                .map(ref -> new SourceEdit(ref.range().start(), ref.range().end(), to, EditMode.Replace))
                .forEachOrdered(results::add);
        return new SourceEditListResponse(true, "OK", results);
    }

    ReferencesResponse references(TopLevelNode root, String from);

    void forEachComment(ISourceFile sourceFile, Consumer<CommentRec> consumer);
}
