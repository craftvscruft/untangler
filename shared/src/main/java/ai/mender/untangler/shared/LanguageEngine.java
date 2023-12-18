package ai.mender.untangler.shared;

import ai.mender.untangler.shared.response.*;

import java.util.List;
import java.util.stream.Stream;

public interface LanguageEngine {
    Stream<FunctionRec> functions(ISourceFile sourceFile);
    Stream<CommentRec> comments(ISourceFile sourceFile);

    default SourceEditListResponse insertComment(int line, String text) {
        String commentText = formatMultiLineComment(text.trim());
        SourceEdit edit = SourceEdit.insert(new SourcePosition(line, 1), commentText);
        return new SourceEditListResponse(true, "OK", List.of(edit));
    }

    String formatMultiLineComment(String text);
}
