package ai.mender.commands;

import ai.mender.Console;
import ai.mender.SimpleSelector;
import ai.mender.domain.ReferencesResponse;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(
        name = "references",
        mixinStandardHelpOptions = true,
        description = "Get references to a name in a file",
        aliases = {"refs", "ref"})
public class GetReferencesCmd implements Runnable, CommandLine.IExitCodeGenerator {


    @CommandLine.Parameters(index = "0", description = "Name of a symbol, line number, or 'name:line'")
    private String selector;

    @CommandLine.Option(
            names = {"--file", "-f"},
            description = "The source code file to analyze",
            required = true,
            defaultValue = "${env:UNTANGLER_DEFAULT_FILE}"
    )
    private File file;
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
                ReferencesResponse response = languageStrategy.references(tree, SimpleSelector.parse(selector));

                Console.printOutput(response, spec.commandLine().getOut(), outputFormat);
                success = true;
                message = "OK";
            }
        } catch (Exception e) {
            message = e.getMessage();
            success = false;
            e.printStackTrace();
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
