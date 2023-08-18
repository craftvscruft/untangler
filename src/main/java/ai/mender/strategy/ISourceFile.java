package ai.mender.strategy;

import org.antlr.v4.runtime.CharStream;

public interface ISourceFile {
    CharStream getCharStream();

    LanguageStrategy createStrategyForFile();
}
