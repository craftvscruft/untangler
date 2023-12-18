package ai.mender.untangler.shared.response;

import ai.mender.untangler.shared.TableOutput;

import java.io.PrintWriter;
import java.util.List;

public record SourceEditListResponse(boolean success, String message, List<SourceEdit> edits) {
    public void writeTableOutput(PrintWriter writer) {
        TableOutput.writeTableOutput(writer, this.edits, SourceEdit.class);
    }
}
