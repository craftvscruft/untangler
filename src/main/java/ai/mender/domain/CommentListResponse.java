package ai.mender.domain;

import ai.mender.untangler.shared.TableOutput;

import java.io.PrintWriter;
import java.util.List;

public record CommentListResponse(boolean success, String message, List<CommentRec> items) {
    public void writeTableOutput(PrintWriter writer) {
        TableOutput.writeTableOutput(writer, this.items, CommentRec.class);
    }
}
