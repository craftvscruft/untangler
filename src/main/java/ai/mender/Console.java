package ai.mender;


import ai.mender.commands.OutputFormat;
import ai.mender.domain.*;
import ai.mender.output.TableOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.PrintWriter;

public class Console {
    private static ObjectMapper JSON_MAPPER = JsonMapper.builder()
            .build().setDefaultPrettyPrinter(new JsonPrettyPrinter());

    private static ObjectMapper YAML_MAPPER = YAMLMapper.builder()
            .build();
    public static <T> void printOutput(T value, PrintWriter out, OutputFormat outputFormat) {
        switch (outputFormat) {
            case json -> toJson(out, value);
            case yaml -> toYaml(out, value);
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
        } else if (value instanceof ReferencesResponse referencesResponse) {
            out.println("Declarations");
            TableOutput.writeTableOutput(out, referencesResponse.declarations(), SourceRange.class);
            out.println();
            out.println("References");
            TableOutput.writeTableOutput(out, referencesResponse.references(), Reference.class);
        } else if (value instanceof CommentListResponse commentListResponse) {
            TableOutput.writeTableOutput(out, commentListResponse.items(), CommentRec.class);
            out.println();
        }else {
            // TODO: Test
            if (null != value) {
                out.println(value);
            }

        }
    }

    public static <T extends Object> void toJson(PrintWriter out, T object) {
        try {
            ObjectWriter writer = JSON_MAPPER.writerWithDefaultPrettyPrinter();
            writer.writeValue(out, object);
            out.println();
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Object> void toYaml(PrintWriter out, T object) {
        try {
            ObjectWriter writer = YAML_MAPPER.writerWithDefaultPrettyPrinter();
            writer.writeValue(out, object);
            out.println();
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
