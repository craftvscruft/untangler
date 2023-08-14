package ai.mender;


import ai.mender.commands.OutputFormat;
import ai.mender.domain.FunctionListResponse;
import ai.mender.domain.SourceEditListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.PrintWriter;

public class Console {
    private static ObjectMapper MAPPER = JsonMapper.builder()
            .build().setDefaultPrettyPrinter(new JsonPrettyPrinter());
    public static <T> void printOutput(T value, PrintWriter out, OutputFormat outputFormat) {
        switch (outputFormat) {
            case json -> toJson(out, value);
            case text -> toText(out, value);
        }
    }

    private static <T> void toText(PrintWriter out, T value) {
        if (value instanceof FunctionListResponse listResponse) {
            listResponse.writeTableOutput(out);
            if (!listResponse.success()) {
                out.println(listResponse.message());
            }
        } else if (value instanceof SourceEditListResponse listResponse) {
            listResponse.writeTableOutput(out);
            if (!listResponse.success()) {
                out.println(listResponse.message());
            }
        } else {
            out.println(value.toString());
        }
    }

    public static <T extends Object> void toJson(PrintWriter out, T object) {
        try {
            ObjectWriter writer = MAPPER.writerWithDefaultPrettyPrinter();
            writer.writeValue(out, object);
            out.println();
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
