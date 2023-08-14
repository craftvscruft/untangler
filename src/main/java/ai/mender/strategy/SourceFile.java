package ai.mender.strategy;

import ai.mender.parsing.CharsetUtils;
import ai.mender.strategy.cpp.CppStrategy;
import ai.mender.strategy.csharp.CSharpStrategy;
import ai.mender.strategy.java.JavaStrategy;
import ai.mender.strategy.python.PythonStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import java.io.File;
import java.io.IOException;

public record SourceFile(File file) {
    static String getExtension(File file) {
        var extension = "";
        String[] split = file.getName().split("\\.");
        if (split.length > 1) {
            extension = split[split.length - 1];
        }
        return extension;
    }

    public CharStream getCharStream() {
        try {
            return CharStreams.fromFileName(
                    file().getAbsolutePath(), CharsetUtils.detectFileCharset(file()));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public LanguageStrategy createStrategyForFile() {
        return switch (getExtension(file()).toLowerCase()) {
            case "c", "cpp", "cc" -> new CppStrategy();
            case "py" -> new PythonStrategy();
            case "java" -> new JavaStrategy();
            case "cs" -> new CSharpStrategy();
            default -> null;
        };
    }

}