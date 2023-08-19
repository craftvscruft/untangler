package ai.mender.strategy;

import ai.mender.SimpleSelector;
import ai.mender.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface LanguageStrategy {
    TopLevelNode parseTopLevel(ISourceFile sourceFile);

    default SourceEditListResponse rename(TopLevelNode root, SimpleSelector fromSelector, String to) {


        var referencesResponse = references(root, fromSelector);
        List<SourceRange> declarations = referencesResponse.declarations();
        if (fromSelector.hasLine()) {
            declarations = declarations.stream()
                    .filter(sourceRange -> sourceRange.start().line() == fromSelector.line())
                    .toList();
        }
        if (declarations.isEmpty()) {
            return new SourceEditListResponse(false, "No matching declarations found in file", Arrays.asList());
        }
        if (declarations.size() > 1) {
            String options = declarations.stream().map(decl -> String.format("%s:%s", fromSelector.name(), decl.start().line())).collect(Collectors.joining("\n"));
            String message = "Multiple matches, please specify with <name>:<line>, one of:\n" + options;
            return new SourceEditListResponse(false, message, Arrays.asList());
        }
        SourceRange declarationRange = declarations.get(0);
        var results = new ArrayList<SourceEdit>();
        results.add(new SourceEdit(declarationRange.start(), declarationRange.end(), EditMode.Replace, to));
        referencesResponse.references().stream()
                .filter(ref -> declarationRange.equals(ref.declarationRange()))
                .map(ref -> new SourceEdit(ref.range().start(), ref.range().end(), EditMode.Replace, to))
                .forEachOrdered(results::add);
        return new SourceEditListResponse(true, "OK", results);
    }

    ReferencesResponse references(TopLevelNode root, SimpleSelector selector);

    void forEachComment(ISourceFile sourceFile, Consumer<CommentRec> consumer);
}
