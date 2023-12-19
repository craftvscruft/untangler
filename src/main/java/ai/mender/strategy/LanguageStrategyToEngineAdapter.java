package ai.mender.strategy;

import ai.mender.untangler.shared.ISourceFile;
import ai.mender.untangler.shared.LanguageEngine;
import ai.mender.untangler.shared.SimpleSelector;
import ai.mender.untangler.shared.response.CommentRec;
import ai.mender.untangler.shared.response.FunctionRec;
import ai.mender.untangler.shared.response.ReferencesResponse;
import ai.mender.untangler.shared.response.SourceEditListResponse;

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

    @Override
    public Stream<CommentRec> comments(ISourceFile sourceFile) {
        List<CommentRec> items = new ArrayList<>();
        strategy.forEachComment(sourceFile, items::add);
        return items.stream();
    }

    @Override
    public String formatMultiLineComment(String text) {
        return strategy.formatMultiLineComment(text);
    }

    @Override
    public ReferencesResponse references(ISourceFile sourceFile, SimpleSelector selector) {
        TopLevelNode tree = strategy.parseTopLevel(sourceFile);
        return strategy.references(tree, selector);
    }

    @Override
    public SourceEditListResponse rename(ISourceFile sourceFile, SimpleSelector fromSelector, String to) {
        TopLevelNode tree = strategy.parseTopLevel(sourceFile);
        return LanguageEngine.renameInner(fromSelector, to, strategy.references(tree, fromSelector));
    }
}
