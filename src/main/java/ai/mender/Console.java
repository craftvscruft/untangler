package ai.mender;


import ai.mender.commands.OutputFormat;
import ai.mender.domain.FunctionListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Console {
    private static ObjectMapper MAPPER = JsonMapper.builder()
            .build().setDefaultPrettyPrinter(new JsonPrettyPrinter());
    public static <T> void printOutput(T result, PrintWriter out, OutputFormat outputFormat) {
        switch (outputFormat) {
            case json -> out.println(toJson(result));
            case text -> out.println(toText(result));
        }
    }

    private static <T> String toText(T value) {
        if (value instanceof FunctionListResponse listResponse) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            listResponse.writeTableOutput(printWriter);
            if (!listResponse.success()) {
                printWriter.println(listResponse.message());
            }
            printWriter.flush();
            return writer.toString();
        }
        return value.toString();
    }

    public static <T extends Object> String toJson(T object) {
        try {
            ObjectWriter writer = MAPPER.writerWithDefaultPrettyPrinter();
            return writer.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
