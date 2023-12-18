package ai.mender.untangler.shared.response;

import ai.mender.untangler.shared.TableOutput;

import java.io.PrintWriter;
import java.util.List;

public record FunctionListResponse (boolean success, String message, List<FunctionRec> items) {
    public void writeTableOutput(PrintWriter writer) {
        TableOutput.writeTableOutput(writer, this.items, FunctionRec.class);
    }
}
