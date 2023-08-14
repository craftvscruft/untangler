package ai.mender.commands;

import ai.mender.Console;
import ai.mender.domain.FunctionRec;
import ai.mender.domain.ListResponse;
import ai.mender.strategy.LanguageStrategies;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;

@CommandLine.Command(
        name = "functions",
        mixinStandardHelpOptions = true,
        description = "Lists functions defined in a file as json",
        aliases = {"function", "fns", "fn", "fun"})
public class GetFunctionsCmd implements Runnable, CommandLine.IExitCodeGenerator {
    @CommandLine.Parameters(index = "0", description = "The source code file to analyze")
    private File file;

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;
    private boolean success = false;

    @Override
    public void run() {
        var items = new ArrayList<FunctionRec>();
        var message = "OK";
        LanguageStrategy languageStrategy = null;
        try {
            languageStrategy = LanguageStrategies.createStrategyForFile(new SourceFile(file));
            if (languageStrategy != null) {
                languageStrategy.collectFunctions(file, items, false);
                success = true;
            } else {
                message = "Unknown file type! Cannot parse.";
                success = false;
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        ListResponse<FunctionRec> response = new ListResponse<>(success, message, items);
        Console.printJson(response, spec.commandLine().getOut());
    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
