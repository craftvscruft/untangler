package ai.mender;

import ai.mender.untangler.shared.SourceRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSelector {
    @Test
    public void starMatchesAll() {
        SimpleSelector subject = SimpleSelector.parse("*");
        Assertions.assertTrue(subject.matchesLineRange(SourceRange.parse("1:0-1:0")));
        Assertions.assertTrue(subject.matchesLineRange(SourceRange.parse("2:0-3:0")));
        Assertions.assertTrue(subject.matchesName("foo"));
    }

    @Test
    public void name() {
        SimpleSelector subject = SimpleSelector.parse("foo");
        Assertions.assertTrue(subject.matchesLineRange(SourceRange.parse("1:0-1:0")));
        Assertions.assertTrue(subject.matchesName("foo"));
        Assertions.assertFalse(subject.matchesName("bar"));
    }

    @Test
    public void line() {
        SimpleSelector subject = SimpleSelector.parse("*:2");
        Assertions.assertTrue(subject.matchesLineRange(SourceRange.parse("2:0-2:0")));
        Assertions.assertFalse(subject.matchesLineRange(SourceRange.parse("1:0-1:0")));
        Assertions.assertTrue(subject.matchesName("foo"));
    }

    @Test
    public void lineRange() {
        SimpleSelector subject = SimpleSelector.parse("*:2-4");
        Assertions.assertTrue(subject.matchesLineRange(SourceRange.parse("2:0-2:0")));
        Assertions.assertTrue(subject.matchesLineRange(SourceRange.parse("3:0-3:0")));
        Assertions.assertTrue(subject.matchesLineRange(SourceRange.parse("4:0-4:0")));
        Assertions.assertFalse(subject.matchesLineRange(SourceRange.parse("1:0-1:0")));
        Assertions.assertFalse(subject.matchesLineRange(SourceRange.parse("5:0-5:0")));
        Assertions.assertTrue(subject.matchesName("foo"));
    }
}
