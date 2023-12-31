package ai.mender.commands;

import ai.mender.Console;
import ai.mender.strategy.LanguageEngineFactory;
import ai.mender.untangler.shared.LanguageEngine;
import ai.mender.untangler.shared.SimpleSelector;
import ai.mender.untangler.shared.response.ReferencesResponse;
import ai.mender.strategy.SourceFile;
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
            LanguageEngine engine = LanguageEngineFactory.forSource(sourceFile);
            if (!file.exists()) {
                message = "File not found";
                success = false;
            } else if (engine == null) {
                message = "Unknown file type! Cannot parse.";
                success = false;
            } else {

                SimpleSelector simpleSelector = SimpleSelector.parse(selector);
                ReferencesResponse response = engine.references(sourceFile, simpleSelector);
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
