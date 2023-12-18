package ai.mender.strategy;

import ai.mender.untangler.javaparser.JavaParserEngine;
import ai.mender.untangler.shared.LanguageEngine;

// This will eventually replace LanguageStrategy
public interface LanguageEngineFactory {
    static LanguageEngine forSource(SourceFile sourceFile) {
        return switch (sourceFile.getExtension().toLowerCase()) {
            case "java" -> new JavaParserEngine();
            default -> {
                LanguageStrategy strategyForFile = sourceFile.createStrategyForFile();
                if (strategyForFile == null) {
                    yield null;
                }
                yield new LanguageStrategyToEngineAdapter(strategyForFile);
            }
        };
    }
}
