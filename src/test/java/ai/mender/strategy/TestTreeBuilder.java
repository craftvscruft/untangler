package ai.mender.strategy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTreeBuilder {

    private TreeBuilder subject;
    @BeforeEach
    void setup() {
        subject = new TreeBuilder();
    }
    @Test
    void test() {
        subject.enter("list");
        subject.exit();
        Assertions.assertEquals("(root list)", subject.toSexpr());
    }

    @Test
    void testAdd() {
        subject.enter("list");
        subject.enter("1").exit();
        subject.enter("2").exit();
        subject.exit();
        Assertions.assertEquals("(root\n  (list 1 2))", subject.toSexpr());
    }

    @Test
    void testAddWithEnterExit() {
        subject.enter("list");
        subject.addAtom("1");
        subject.addAtom("2");
        subject.exit();
        Assertions.assertEquals("(root\n  (list 1 2))", subject.toSexpr());
    }

    @Test
    void testAddNested() {
        subject.enter("list");
        subject.addAtom("1");
        subject.enter("inner").addAtom("2").addAtom("3").exit();
        subject.exit();
        String expected = """
        (root
          (list
            1
            (inner 2 3)))""";
        Assertions.assertEquals(expected, subject.toSexpr());
    }

    @Test
    void testAccumulateAvoidsDuplicateTags() {
        subject.enter("list");
        subject.enter("inner").addAtom("2").addAtom("3").exit();
        subject.accumulate("inner", "4");
        subject.exit();
        String expected = """
        (root
          (list
            (inner 2 3 4)))""";
        Assertions.assertEquals(expected, subject.toSexpr());
    }
}
