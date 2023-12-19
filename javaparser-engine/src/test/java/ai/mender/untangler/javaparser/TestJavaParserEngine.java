package ai.mender.untangler.javaparser;

import ai.mender.untangler.shared.SimpleSelector;
import ai.mender.untangler.shared.StringSourceFile;
import ai.mender.untangler.shared.response.CommentRec;
import ai.mender.untangler.shared.response.ReferencesResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TestJavaParserEngine {
    @Test
    public void singleLineComments() {
        JavaParserEngine subject = new JavaParserEngine();
        Stream<CommentRec> comments = subject.comments(new StringSourceFile("a.java", "// Comment"));
        Assertions.assertEquals(1, comments.count());
    }

    @Test
    public void multiLineComments() {
        JavaParserEngine subject = new JavaParserEngine();
        Stream<CommentRec> comments = subject.comments(new StringSourceFile("a.java", "/* Comment \n 2 */"));
        Assertions.assertEquals(1, comments.count());
    }

    @Test
    public void multiLineComments2() {
        JavaParserEngine subject = new JavaParserEngine();
        Stream<CommentRec> comments = subject.comments(new StringSourceFile("a.java", "class /* Comment \n 2 */ Foo {}"));
        Assertions.assertEquals(1, comments.count());
    }

    @Test
    public void references() throws IOException {
        JavaParserEngine subject = new JavaParserEngine();
        StringSourceFile file = new StringSourceFile("a.java", """
        class Foo {
            int foo() {
                int f = 1;
                int g = f + f;
            }
        }
        """);
        ReferencesResponse actual = subject.references(file, SimpleSelector.parse("f"));
        Assertions.assertEquals(2, actual.references().size());
        Assertions.assertEquals(1, actual.declarations().size());
    }
}
