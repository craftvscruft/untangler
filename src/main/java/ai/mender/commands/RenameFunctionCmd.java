package ai.mender.commands;

import ai.mender.Console;
import ai.mender.domain.SourceEditListResponse;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
        name = "function",
        mixinStandardHelpOptions = true,
        description = "Rename function in file",
        aliases = "fn")
public class RenameFunctionCmd implements Runnable, CommandLine.IExitCodeGenerator {
    @CommandLine.Parameters(index = "0", description = "The source code file to analyze")
    private File file;

    @CommandLine.Option(names = "--from", description = "The old name")
    private String from;

    @CommandLine.Option(names = "--to", description = "The new name")
    private String to;
    @CommandLine.Option(names = {"--output", "-o"}, defaultValue = "text",
            description = {
                    "Output format",
                    "Supported: text (default), json"
            })
    private OutputFormat outputFormat;

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
                SourceEditListResponse response = tree.renameFunction(from, to);
                Console.printOutput(response, spec.commandLine().getOut(), outputFormat);
                success = response.success();
                message = "";
            }
        } catch (Exception e) {
            message = e.getMessage();
            success = false;
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
