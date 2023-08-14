package ai.mender.strategy;

import ai.mender.domain.FunctionRec;

import java.util.List;

public interface TopLevelNode {
    void collectFunctions(List<FunctionRec> items);
}
