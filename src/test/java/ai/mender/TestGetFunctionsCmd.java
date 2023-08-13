package ai.mender;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class TestGetFunctionsCmd {

    private Main app;
    private CommandLine cmd;
    private StringWriter outWriter;
    private StringWriter errWriter;

    @BeforeEach
    public void setup() {
        app = new Main();
        cmd = new CommandLine(app);
        outWriter = new StringWriter();
        errWriter = new StringWriter();
        cmd.setOut(new PrintWriter(outWriter));
        cmd.setErr(new PrintWriter(errWriter));
    }

    private String getTestResourcePath(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        String helloCPath = file.getAbsolutePath();
        return helloCPath;
    }

    @Test
    public void testCFile() {
        int exitCode = cmd.execute("get", "fns", getTestResourcePath("hello.c"));
        Assertions.assertEquals(0, exitCode);
        String out = outWriter.toString();
        String err = errWriter.toString();
        Assertions.assertEquals("", err);
        Approvals.verify(out);
    }

    @Test
    public void testUnknownFileType() {
        int exitCode = cmd.execute("get", "fns", "build.gradle");
        Assertions.assertEquals(1, exitCode);
    }
}
