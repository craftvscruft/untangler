package ai.mender;

import ai.mender.domain.EditMode;
import ai.mender.domain.SourceEdit;
import ai.mender.domain.SourcePosition;
import ai.mender.strategy.SourceFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class TestSourceFile {
    @Test
    public void applyUpdatesBeginning() throws IOException {
        String before = "";
        String expectedAfter = "<new>";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(1, 1),
                        sp(1, 1), "<new>", EditMode.Replace));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }

    @Test
    public void applyUpdatesMidLine() throws IOException {
        String before = "123456";
        String expectedAfter = "123<new>456";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(4, 1),
                        sp(4, 1), "<new>", EditMode.Replace));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }

    @Test
    public void applyMultipleUpdatesSameLine() throws IOException {
        String before = "123456";
        String expectedAfter = "1<low>2345<high>6";
        SourceEdit edit1 = new SourceEdit(sp(2, 1),
                sp(2, 1), "<low>", EditMode.Replace);
        SourceEdit edit2 = new SourceEdit(sp(6, 1),
                sp(6, 1), "<high>", EditMode.Replace);
        List<SourceEdit> edits = List.of(
                edit1, edit2);

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }

    @Test
    public void applyUpdatesSecondLine() throws IOException {
        String before = "123\n456\n";
        String expectedAfter = "123\n321\n";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(1, 2),
                        sp(4, 2), "321", EditMode.Replace));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }


    @Test
    public void replaceContent() throws IOException {
        String before = "abcd";
        String expectedAfter = "a__d";
        List<SourceEdit> edits = List.of(
                new SourceEdit(sp(2, 1),
                        sp(4, 1), "__", EditMode.Replace));

        Assertions.assertEquals(expectedAfter, applyEdits(before, edits));
    }

    private static SourcePosition sp(int col, int line) {
        return new SourcePosition(line, col);
    }


    private static String applyEdits(String before, List<SourceEdit> edits) throws IOException {
        StringReader reader = new StringReader(before);

        StringWriter writer = new StringWriter();

        SourceFile.applyEdits(edits, reader, writer);
        return writer.toString();
    }
}
