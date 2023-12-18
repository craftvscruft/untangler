package ai.mender.strategy;

import ai.mender.untangler.shared.ISourceFile;
import ai.mender.untangler.shared.LanguageEngine;
import ai.mender.untangler.shared.response.FunctionRec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LanguageStrategyToEngineAdapter implements LanguageEngine  {
    private final LanguageStrategy strategy;

    public LanguageStrategyToEngineAdapter(LanguageStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Stream<FunctionRec> functions(ISourceFile sourceFile) {
        List<FunctionRec> items = new ArrayList<>();
        strategy.parseTopLevel(sourceFile).collectFunctions(items);
        return items.stream();
    }
}
