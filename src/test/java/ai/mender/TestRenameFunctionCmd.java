package ai.mender;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class TestRenameFunctionCmd {
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
    @ValueSource(strings = {"hello.c_main", "hello.cpp_main", "hello.py_hello", "Hello.java_main", "Hello.cs_Main"})
    public void testRenameSimpleFileJsonOutput(String fileNameFnName) {
        String[] split = fileNameFnName.split("_");
        String fileName = split[0];
        String fnName = split[1];
        int exitCode = cmd.execute("ren", "fn", "-f", getTestResourcePath(fileName), fnName, fnName+2,"-o", "json");
        Assertions.assertEquals(0, exitCode);
        String out = outWriter.toString();
        String err = errWriter.toString();
        Assertions.assertEquals("", err);
        Approvals.verify(out, Approvals.NAMES.withParameters(fileName));
    }
}
