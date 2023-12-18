package ai.mender.untangler.javaparser;

import ai.mender.untangler.shared.StringSourceFile;
import ai.mender.untangler.shared.response.CommentRec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
