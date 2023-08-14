package ai.mender;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.PrintWriter;

public class Console {
    private static ObjectMapper MAPPER = JsonMapper.builder()
            .build().setDefaultPrettyPrinter(new JsonPrettyPrinter());
    public static <T> void printJson(T result, PrintWriter out) {
        out.println(toJson(result));
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
