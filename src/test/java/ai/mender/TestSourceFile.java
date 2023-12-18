package ai.mender;

import ai.mender.untangler.shared.response.EditMode;
import ai.mender.untangler.shared.response.SourceEdit;;
import ai.mender.strategy.SourceFile;
import ai.mender.untangler.shared.response.SourcePosition;
import ai.mender.untangler.shared.response.SourceRange;
import com.google.common.collect.Ordering;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;

public class TestSourceFile {
    @Test
    public void applyUpdatesBeginning() throws IOException {
        String before = "";
        String expectedAfter = "<new>";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(1, 1),
                        sp(1, 1), EditMode.Replace, "<new>"));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }

    @Test
    public void applyUpdatesMidLine() throws IOException {
        String before = "123456";
        String expectedAfter = "123<new>456";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(1, 4),
                        sp(1, 4), EditMode.Replace, "<new>"));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }

    @Test
    public void applyMultipleUpdatesSameLine() throws IOException {
        String before = "123456";
        String expectedAfter = "1<low>2345<high>6";
        SourceEdit edit1 = new SourceEdit(sp(1, 2),
                sp(1, 2), EditMode.Replace, "<low>");
        SourceEdit edit2 = new SourceEdit(sp(1, 6),
                sp(1, 6), EditMode.Replace, "<high>");
        List<SourceEdit> edits = List.of(
                edit1, edit2);

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }

    @Test
    public void applyUpdatesSecondLine() throws IOException {
        String before = "123\n456\n";
        String expectedAfter = "123\n321\n";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(2, 1),
                        sp(2, 4), EditMode.Replace, "321"));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }

    @Test
    public void addMultipleLinesInARow() throws IOException {
        String before = "123\n6\n";
        String expectedAfter = "123\n4\n5\n6\n";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(2, 1),
                        sp(2, 1), EditMode.Insert, "4\n"),
                new SourceEdit(sp(2, 1),
                        sp(2, 1), EditMode.Insert, "5\n"));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }

    @Test
    public void addLinesAfterDeletingOne() throws IOException {
        String before = "123\n6\n";
        String expectedAfter = "\n4\n5\n6\n";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(1, 1),
                        sp(1, 4), EditMode.Delete, ""),
                new SourceEdit(sp(2, 1),
                        sp(2, 1), EditMode.Insert, "4\n"),
                new SourceEdit(sp(2, 1),
                        sp(2, 1), EditMode.Insert, "5\n"));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }


    @Test
    public void replaceContent() throws IOException {
        String before = "abcd";
        String expectedAfter = "a__d";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(1, 2),
                        sp(1, 4), EditMode.Replace, "__"));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }


    @Test
    public void overlapRegression() throws IOException {
        List<SourceEdit> edits = List.of(
                SourceEdit.replace(SourceRange.parse("3:49-3:50"), "x"),
                SourceEdit.replace(SourceRange.parse("21:109-21:110"), "x"),
                SourceEdit.replace(SourceRange.parse("22:75-22:76"), "x"),
                SourceEdit.replace(SourceRange.parse("22:79-22:80"), "x"),
                SourceEdit.replace(SourceRange.parse("23:24-23:25"), "x"),
                SourceEdit.replace(SourceRange.parse("24:70-24:71"), "x"),
                SourceEdit.replace(SourceRange.parse("69:25-69:26"), "x"),
                SourceEdit.replace(SourceRange.parse("66:13-66:14"), "x"));
        String code = "                                                                   \n".repeat(70);

        var ordering = Ordering.from(Comparator.comparing((SourceEdit e) -> e.start()));
        SourceEdit bigger = edits.get(6);
        SourceEdit smaller = edits.get(7);
        Assertions.assertEquals(1, ordering.compare(bigger, smaller), bigger + "should be > " +smaller);
        applyEdits(code, edits);
    }

    @Test
    public void outOfOrderRegression() {
        List<SourceEdit> edits = List.of(
                SourceEdit.replace(SourceRange.parse("66:13-66:14"), "x"),
                SourceEdit.replace(SourceRange.parse("69:25-69:26"), "x"));
        String code = "                                                                   \n".repeat(70);
        Assertions.assertDoesNotThrow(()-> {
            applyEdits(code, edits);
        });
    }

    private static SourcePosition sp(int line, int col) {
        return new SourcePosition(line, col);
    }


    private static String applyEdits(String before, List<SourceEdit> inputEdits) throws IOException {
        StringReader reader = new StringReader(before);
        StringWriter writer = new StringWriter();
        var edits = SourceFile.sortedEditsCheckedForDupes(inputEdits);
        SourceFile.applyEdits(edits, reader, writer);
        return writer.toString();
    }
}
