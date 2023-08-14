package ai.mender.domain;

import ai.mender.output.TableOutput;

import java.io.Writer;
import java.util.List;

public record FunctionListResponse (boolean success, String message, List<FunctionRec> items) {
    public void writeTableOutput(Writer writer) {
        TableOutput.writeTableOutput(writer, this.items, FunctionRec.class);
    }
}
