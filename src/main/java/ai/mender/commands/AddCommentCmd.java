package ai.mender.commands;

import ai.mender.Console;
import ai.mender.domain.SourceEdit;
import ai.mender.domain.SourceEditListResponse;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

@CommandLine.Command(
        name = "comment",
        mixinStandardHelpOptions = true,
        description = "Inserts a comment",
        aliases = {"com"})
public class AddCommentCmd implements Runnable, CommandLine.IExitCodeGenerator {

    @CommandLine.Parameters(index = "0", description = "line")
    private int line;

    @CommandLine.Parameters(index = "1", description = "text")
    private String text;
    @CommandLine.Option(
            names = {"--file", "-f"},
            description = "The source code file to analyze",
            required = true,
            defaultValue = "${env:UNTANGLER_DEFAULT_FILE}")
    private File file;

    @CommandLine.Option(names = {"--output", "-o"}, defaultValue = "text",
            description = {
                    "Output format",
                    "Supported: text (default), json"
            })
    private OutputFormat outputFormat;
    @CommandLine.Option(
            names = {"--write", "-w"},
            description = "Write the modified version"
    )
    private boolean write;

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;
    private boolean success = false;

    @Override
    public void run() {
        var message = "OK";
        try {
            SourceFile sourceFile = new SourceFile(file);
            LanguageStrategy languageStrategy = sourceFile.createStrategyForFile();
            if (!file.exists()) {
                message = "File not found";
                success = false;
            } else if (languageStrategy == null) {
                message = "Unknown file type! Cannot parse.";
                success = false;
            } else {
                TopLevelNode tree = languageStrategy.parseTopLevel(sourceFile);
                SourceEditListResponse response = languageStrategy.insertComment(tree, line, text);
                Console.printOutput(response, spec.commandLine().getOut(), outputFormat);
                success = response.success();
                if (success && write) {
                    sourceFile.update(file, response.edits(), spec.commandLine().getErr());
                }
            }
        } catch (Exception e) {
            success = false;
            message = e.getClass() + " " + e.getMessage();
        }
        if (!success) {
            Console.printOutput(message, spec.commandLine().getOut(), outputFormat);
        }

    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
