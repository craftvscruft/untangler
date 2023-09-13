package ai.mender.commands;

import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.untangler.clang.ClangRunner;
import picocli.CommandLine;

import java.io.File;


@CommandLine.Command(name = "expressions", mixinStandardHelpOptions = true,
        aliases = "expr",
        description = "Display expressions within a function")
public class GetExpressions implements Runnable, CommandLine.IExitCodeGenerator {

    @CommandLine.Parameters(index = "0", description = "selector (try a function name)", defaultValue = "*")
    private String selector;
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
//                languageStrategy.forEachComment(sourceFile, comment -> {
//                    if (Console.isLineMatch(comment.range(), line)) {
//                        items.add(comment);
//                    }
//                });
                String filePath = file.getAbsolutePath();
                ClangRunner.runClang(this.selector, filePath);

                success = true;
            }
        } catch (Exception e) {
            message = e.getMessage();

        }
        spec.commandLine().getOut().println(message);
    }

    @Override
    public int getExitCode() {
        return success ? 0 : 1;
    }
}
