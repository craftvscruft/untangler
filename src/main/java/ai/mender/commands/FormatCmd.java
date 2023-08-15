package ai.mender.commands;

import ai.mender.strategy.SourceFile;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@CommandLine.Command(
        name = "format",
        mixinStandardHelpOptions = true,
        description = "Formats a file with the configured formatter",
        aliases = {"fmt"})
public class FormatCmd implements Runnable, CommandLine.IExitCodeGenerator {
    @CommandLine.Option(
            names = {"--file", "-f"},
            description = "The source code file to format",
            required = true,
            defaultValue = "${env:UNTANGLER_DEFAULT_FILE}")
    private File file;

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;
    private boolean success = false;
    @Override
    public void run() {
        try {

            String extension = SourceFile.getExtension(file);
            String extUpCase = extension.toUpperCase();
            String formatCommand = System.getenv("UNTANGLER_" + extUpCase + "_FORMAT_EXEC");
            if (null == formatCommand || "".equals(formatCommand)) {
                System.out.println("Please set UNTANGLER_C_FORMAT_EXEC");
                return;
            }
            File tempFile = Files.createTempFile(file.getName(), ".tmp").toFile();
            Process start = new ProcessBuilder()
                    .command(formatCommand, file.getAbsolutePath())
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(tempFile)
                    .start();
            success = start.waitFor() == 0;
            if (success) {
                System.out.println("Saved to tmp: " + tempFile);
                System.out.println("Updating " + file.getName());
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println(e.getClass() + ": " + e.getMessage());
            success = false;
        }
    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
