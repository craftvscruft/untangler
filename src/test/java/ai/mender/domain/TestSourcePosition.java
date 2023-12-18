package ai.mender.domain;

import ai.mender.untangler.shared.response.SourcePosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSourcePosition {
    @Test
    void compare() {
        var a = new SourcePosition(10, 20);
        var b = new SourcePosition(12, 10);
        Assertions.assertEquals(-1, a.compareTo(b));
        Assertions.assertEquals(1, b.compareTo(a));
        Assertions.assertEquals(0, a.compareTo(a));
    }
}
