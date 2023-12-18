package ai.mender.commands;

import ai.mender.Console;
import ai.mender.strategy.LanguageEngineFactory;
import ai.mender.untangler.shared.LanguageEngine;
import ai.mender.untangler.shared.response.CommentRec;
import ai.mender.untangler.shared.response.EditMode;
import ai.mender.untangler.shared.response.SourceEdit;
import ai.mender.untangler.shared.response.SourceEditListResponse;
import ai.mender.strategy.SourceFile;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;

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
    @CommandLine.Option(
            names = {"--sub"},
            description = "Optional string to replace it with, such as space",
            defaultValue = "")
    private String sub;
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
            LanguageEngine engine = LanguageEngineFactory.forSource(sourceFile);
            if (!file.exists()) {
                message = "File not found";
                success = false;
            } else if (engine == null) {
                message = "Unknown file type! Cannot parse.";
                success = false;
            } else {
                engine.comments(sourceFile).forEach(comment -> {
                    if (Console.isLineMatch(comment.range(), line)) {
                        comments.add(comment);
                    }
                });
                comments.stream().map(comment ->
                        new SourceEdit(comment.range().start(), comment.range().end(), EditMode.Delete, sub))
                        .forEachOrdered(edits::add);
                success = true;
                if (success && write) {
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
