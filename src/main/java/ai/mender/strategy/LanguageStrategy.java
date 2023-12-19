package ai.mender.strategy;

import ai.mender.untangler.shared.LanguageEngine;
import ai.mender.untangler.shared.SimpleSelector;
import ai.mender.untangler.shared.Ast;
import ai.mender.untangler.shared.ISourceFile;
import ai.mender.untangler.shared.response.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface LanguageStrategy {
    TopLevelNode parseTopLevel(ISourceFile sourceFile);

    default SourceEditListResponse renameWithAst(Ast root, SimpleSelector fromSelector, String to) {
        var matchingIds = new HashSet<String>();
        root.dfsStream()
                .filter(ast -> fromSelector.matchesName(ast.props().get("name")) &&
                        fromSelector.matchesLineRange(ast.range()))
                .forEach(ast -> {
                    if (ast.props().containsKey("refId")) {
                        matchingIds.add(ast.props().get("refId"));
                    } else if (ast.props().containsKey("id")) {
                        matchingIds.add(ast.props().get("id"));
                    }
                });
        if (matchingIds.isEmpty()) {
            return new SourceEditListResponse(false, "No matching declarations found in file", Arrays.asList());
        }
        if (matchingIds.size() > 1) {
            String message = "Multiple matches, please specify with <name>:<line>";
            return new SourceEditListResponse(false, message, Arrays.asList());
        }
        String matchingId = matchingIds.iterator().next();
        var edits = root.dfsStream()
                .filter(ast -> matchingId.equals(ast.props().get("id")) || matchingId.equals(ast.props().get("refId")))
                .map(ast -> createRenameEditForAstNode(ast, to))
                .toList();

        return new SourceEditListResponse(true, "OK", edits);
    }

    private static SourceEdit createRenameEditForAstNode(Ast ast, String to) {
        SourceRange range = ast.nameRange() == null ? ast.range() : ast.nameRange();
        return new SourceEdit(range.start(), range.end(), EditMode.Replace, to);
    }

    ReferencesResponse references(TopLevelNode root, SimpleSelector selector);

    void forEachComment(ISourceFile sourceFile, Consumer<CommentRec> consumer);

    default SourceEditListResponse insertComment(TopLevelNode tree, int line, String text) {
        String commentText = formatMultiLineComment(text.trim());
        SourceEdit edit = SourceEdit.insert(new SourcePosition(line, 1), commentText);
        return new SourceEditListResponse(true, "OK", List.of(edit));
    }

    String formatMultiLineComment(String text);


}
