package ai.mender.commands;

import ai.mender.Console;
import ai.mender.domain.CommentListResponse;
import ai.mender.domain.CommentRec;
import ai.mender.domain.FunctionListResponse;
import ai.mender.domain.FunctionRec;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;

@CommandLine.Command(
        name = "comments",
        mixinStandardHelpOptions = true,
        description = "Lists functions defined in a file as json",
        aliases = {"comment", "com"})
public class GetCommentsCmd implements Runnable, CommandLine.IExitCodeGenerator {

    @CommandLine.Parameters(index = "0", description = "line", defaultValue = "*")
    private String line;
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
        var items = new ArrayList<CommentRec>();
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
                languageStrategy.forEachComment(sourceFile, comment -> {
                    if (line == "*" || Integer.parseInt(line) == comment.range().start().line()) {
                        // TODO: pre-check format and allow a range for multi-line comment.
                        items.add(comment);
                    }
                });
                success = true;
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        CommentListResponse response = new CommentListResponse(success, message, items);
        Console.printOutput(response, spec.commandLine().getOut(), outputFormat);
    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
