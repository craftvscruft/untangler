package ai.mender.commands;

import ai.mender.Console;
import ai.mender.domain.*;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.SourceFile;
import ai.mender.strategy.TopLevelNode;
import ai.mender.strategy.cpp.CppStrategy;
import ai.mender.untangler.shared.response.DeclarationRec;
import ai.mender.untangler.shared.response.SourcePosition;
import ai.mender.untangler.shared.response.SourceText;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(
        name = "split-declaration",
        mixinStandardHelpOptions = true,
        description = "Split a compound declaration onto multiple lines",
        aliases = {"split-decl"})
public class SplitDeclarationsCmd implements Runnable, CommandLine.IExitCodeGenerator {

    @CommandLine.Parameters(index = "0", description = "line")
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
        var declarations = new ArrayList<DeclarationRec>();
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
                if (languageStrategy instanceof CppStrategy cppStrategy) {
                    TopLevelNode tree = languageStrategy.parseTopLevel(sourceFile);
                    cppStrategy.forEachVarDeclarationStatement(tree, (DeclarationRec decl) -> {
                        if (Console.isLineMatch(decl.range(), line)) {
                            declarations.add(decl);
                        }
                    });

                    for (var declaration : declarations) {
                        List<SourceText> declarators = declaration.declarators();
                        if (declarators.size() > 1) {
                            edits.add(new SourceEdit(declaration.range().start(), declaration.range().end(), EditMode.Delete, ""));
                            int nextLine = declaration.range().end().line() + 1;
                            for (var declarator : declarators) {
                                SourcePosition insertPoint = new SourcePosition(nextLine, 1);
                                String insertText = String.format("%s %s;\n", declaration.specifier(), declarator.text());
                                edits.add(new SourceEdit(insertPoint, insertPoint, EditMode.Insert, insertText));
                            }
                        }
                    }
                    success = true;
                    if (success && write && !edits.isEmpty()) {
                        sourceFile.update(file, edits, spec.commandLine().getErr());
                    }
                } else {
                    message = "Action not available for this language";
                    success = false;
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
