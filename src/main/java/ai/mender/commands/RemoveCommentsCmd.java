package ai.mender.commands;

import ai.mender.Console;
import ai.mender.domain.*;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(
        name = "comments",
        mixinStandardHelpOptions = true,
        description = "Lists functions defined in a file as json",
        aliases = {"comment", "com"})
public class RemoveCommentsCmd implements Runnable, CommandLine.IExitCodeGenerator {

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
    @CommandLine.Option(
            names = {"--write", "-w"},
            description = "Write the modified version"
    )
    private boolean write;
    @CommandLine.Spec CommandLine.Model.CommandSpec spec;
    private boolean success = false;

    @Override
    public void run() {
        var comments = new ArrayList<CommentRec>();
        var edits = new ArrayList<SourceEdit>();
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
                    if (Console.isLineMatch(comment.range(), line)) {
                        comments.add(comment);
                    }
                });
                comments.stream().map(comment ->
                        new SourceEdit(comment.range().start(), comment.range().end(), "", EditMode.Replace))
                        .forEachOrdered(edits::add);
                success = true;
                if (success && write) {
                    SourceFile.sortEditsCheckDupes(edits);
                    sourceFile.update(file, edits, spec.commandLine().getErr());
                }
            }
        } catch (Exception e) {
            message = e.getMessage();
            success = false;
        }
        var response = new SourceEditListResponse(success, message, edits);
        Console.printOutput(response, spec.commandLine().getOut(), outputFormat);
    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
