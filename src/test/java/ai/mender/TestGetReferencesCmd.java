package ai.mender;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class TestGetReferencesCmd {
    private CommandLine cmd;
    private StringWriter outWriter;
    private StringWriter errWriter;

    @BeforeEach
    public void setup() {
        Main app = new Main();
        cmd = new CommandLine(app);
        outWriter = new StringWriter();
        errWriter = new StringWriter();
        cmd.setOut(new PrintWriter(outWriter));
        cmd.setErr(new PrintWriter(errWriter));
    }

    private String getTestResourcePath(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file.getAbsolutePath();
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello_refs.c"})
    public void getAllRefsByName(String fileName) {
        String selector = "main";
        int exitCode = cmd.execute("get", "refs", selector, "-f", getTestResourcePath(fileName), "-o", "json");
        Assertions.assertEquals(0, exitCode);
        String out = outWriter.toString();
        String err = errWriter.toString();
        Assertions.assertEquals("", err);
        Approvals.verify(out, Approvals.NAMES.withParameters(fileName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello_refs.c"})
    public void getRefsByNameAndDeclarationLine(String fileName) {
        String selector = "main:2";
        int exitCode = cmd.execute("get", "refs", selector, "-f", getTestResourcePath(fileName), "-o", "json");
        Assertions.assertEquals(0, exitCode);
        String out = outWriter.toString();
        String err = errWriter.toString();
        Assertions.assertEquals("", err);
        Approvals.verify(out, Approvals.NAMES.withParameters(fileName));
    }
}
