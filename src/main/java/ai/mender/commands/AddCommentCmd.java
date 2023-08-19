package ai.mender.commands;

import ai.mender.Console;
import ai.mender.SimpleSelector;
import ai.mender.domain.CommentListResponse;
import ai.mender.domain.CommentRec;
import ai.mender.domain.SourceEditListResponse;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;

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
                TopLevelNode tree = languageStrategy.parseTopLevel(sourceFile);
                SourceEditListResponse response = languageStrategy.insertComment(tree, line, text);
                Console.printOutput(response, spec.commandLine().getOut(), outputFormat);
                success = response.success();
            }
        } catch (Exception e) {
            success = false;
            message = e.getMessage();
        }
//        CommentListResponse response = new CommentListResponse(success, message, items);
//        Console.printOutput(response, spec.commandLine().getOut(), outputFormat);
    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
