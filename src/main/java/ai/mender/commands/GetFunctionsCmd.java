package ai.mender.commands;

import ai.mender.Console;
import ai.mender.strategy.LanguageEngineFactory;
import ai.mender.untangler.shared.LanguageEngine;
import ai.mender.untangler.shared.response.FunctionListResponse;
import ai.mender.untangler.shared.response.FunctionRec;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "functions",
        mixinStandardHelpOptions = true,
        description = "Lists functions defined in a file",
        aliases = {"function", "fns", "fn"})
public class GetFunctionsCmd implements Runnable, CommandLine.IExitCodeGenerator {
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

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;
    private boolean success = false;

    @Override
    public void run() {
        var items = new ArrayList<FunctionRec>();
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
                LanguageEngine engine = LanguageEngineFactory.forSource(sourceFile);
                engine.functions(sourceFile).forEach(items::add);
                success = true;
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        FunctionListResponse response = new FunctionListResponse(success, message, items);
        Console.printOutput(response, spec.commandLine().getOut(), outputFormat);
    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
