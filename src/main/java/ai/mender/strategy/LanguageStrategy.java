package ai.mender.strategy;

import ai.mender.domain.FunctionRec;

import java.io.File;
import java.util.List;

public interface LanguageStrategy {
    void collectFunctions(File file, List<FunctionRec> items, boolean throwOnParseError);
}
