package ai.mender.strategy;

public interface LanguageStrategy {
    TopLevelNode parseTopLevel(SourceFile sourceFile);
}
