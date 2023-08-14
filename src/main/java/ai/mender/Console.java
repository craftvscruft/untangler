package ai.mender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.PrintWriter;

public class Console {
    public static <T> void printJson(T result, PrintWriter out) {
        try {
            out.println(toJson(result));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String toJson(T object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper() {};

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper
                .setDefaultPrettyPrinter(new JsonPrettyPrinter())
                .writeValueAsString(object);
    }
}
