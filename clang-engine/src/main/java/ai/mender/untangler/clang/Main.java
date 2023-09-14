package ai.mender.untangler.clang;

import ai.mender.untangler.shared.Ast;
import ai.mender.untangler.shared.SourceRange;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Running...");
        Ast ast = ClangEngine.runClang("/Users/rmyers/dev/ioccc/endoh2/main.c");
        printJsonTree(ast);
    }

    private static void printJsonTree(Ast ast) throws IOException {
        getAstJsonWriter().writeValue(System.out, ast);
    }

    public static ObjectWriter getAstJsonWriter() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new StdSerializer<>(SourceRange.class) {

            @Override
            public void serialize(SourceRange value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString(value.toString());
            }
        });
        ObjectWriter writer = new ObjectMapper().registerModule(module).writer().withDefaultPrettyPrinter();
        return writer;
    }
}
