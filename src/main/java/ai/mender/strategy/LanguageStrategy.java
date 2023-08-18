package ai.mender.strategy;

import ai.mender.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface LanguageStrategy {
    TopLevelNode parseTopLevel(ISourceFile sourceFile);

    default SourceEditListResponse renameFunction(TopLevelNode root, String from, String to) {
        List<FunctionDefinitionNode> matchingFunctions = new ArrayList<>();
        root.forEachFunctionNode(n -> {
            if (n.getName().equals(from)) {
                matchingFunctions.add(n);
            }
        });
        if (matchingFunctions.isEmpty()) {
            return new SourceEditListResponse(false, "No matches", Arrays.asList());
        }
        if (matchingFunctions.size() > 1) {
            return new SourceEditListResponse(false, "Multiple matches", Arrays.asList());
        }

        var refs = references(root, from);
        var results = new ArrayList<SourceEdit>();
        refs.declarations().stream()
                .map(range -> new SourceEdit(range.start(), range.end(), to, EditMode.Replace))
                .forEachOrdered(results::add);
        refs.references().stream()
                .map(ref -> new SourceEdit(ref.range().start(), ref.range().end(), to, EditMode.Replace))
                .forEachOrdered(results::add);
        if (results.isEmpty()) {
            // If there are no results, references is probably not implemented for this language.
            // Rename only the function declaration for now.
            FunctionDefinitionNode target = matchingFunctions.get(0);
            SourceRange nameRange = target.getNameRange();
            results.add(new SourceEdit(nameRange.start(), nameRange.end(), to, EditMode.Replace));
        }
        return new SourceEditListResponse(true, "OK", results);
    }

    ReferencesResponse references(TopLevelNode root, String from);
}
