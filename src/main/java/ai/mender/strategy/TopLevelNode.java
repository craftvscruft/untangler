package ai.mender.strategy;

import ai.mender.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public interface TopLevelNode {

    void forEachFunctionNode(Consumer<FunctionDefinitionNode> consumer);
    default void collectFunctions(List<FunctionRec> items) {
        forEachFunctionNode(node -> items.add(node.toFunctionRec()));
    }

    default SourceEditListResponse renameFunction(String from, String to) {
        List<FunctionDefinitionNode> matchingFunctions = new ArrayList<>();
        forEachFunctionNode(n -> {
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
        FunctionDefinitionNode target = matchingFunctions.get(0);
        SourceRange nameRange = target.getNameRange();
        var edit = new SourceEdit(nameRange.start(), nameRange.end(), to, EditMode.Replace);
        return new SourceEditListResponse(true, "OK", Arrays.asList(edit));
    }

}
