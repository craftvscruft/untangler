package ai.mender.strategy;

import ai.mender.domain.FunctionRec;

import java.util.List;
import java.util.function.Consumer;

public interface TopLevelNode {

    void forEachFunctionNode(Consumer<FunctionDefinitionNode> consumer);
    default void collectFunctions(List<FunctionRec> items) {
        forEachFunctionNode(node -> items.add(node.toFunctionRec()));
    }




}
