package ai.mender;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public class TestJsonPrettyPrinter {
    record Empty() {}

    record Top(List<Bottom> list) {}

    record Bottom(String foo, int bar) {}

    @Test
    public void testEmpty() {
        Approvals.verify(toJsonString(new Empty()));
    }

    private static String toJsonString(Object object) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter out = new PrintWriter(stringWriter);
        Console.toJson(out, object);
        out.flush();
        return stringWriter.toString();
    }

    @Test
    public void testObjectWithListOfObject() {
        // Verify as string not json because we care about the formatting here.
        Bottom rec1 = new Bottom("foo1", 1);
        Bottom rec2 = new Bottom("foo2", 2);
        Top top = new Top(Arrays.asList(rec1, rec2));
        Approvals.verify(toJsonString(top));
    }
}
