package ai.mender;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TestJsonPrettyPrinter {
    record Empty() {}

    record Top(List<Bottom> list) {}

    record Bottom(String foo, int bar) {}

    @Test
    public void testEmpty() {
        // Verify as string not json because we care about the formatting here.
        Approvals.verify(Console.toJson(new Empty()));
    }

    @Test
    public void testObjectWithListOfObject() {
        // Verify as string not json because we care about the formatting here.
        Bottom rec1 = new Bottom("foo1", 1);
        Bottom rec2 = new Bottom("foo2", 2);
        Top top = new Top(Arrays.asList(rec1, rec2));
        Approvals.verify(Console.toJson(top));
    }
}
