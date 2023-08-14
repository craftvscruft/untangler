package ai.mender.strategy;

import ai.mender.strategy.cpp.CppStrategy;
import ai.mender.strategy.csharp.CSharpStrategy;
import ai.mender.strategy.java.JavaStrategy;
import ai.mender.strategy.python.PyStrategy;

import java.io.File;

public record SourceFile(File file) {
    static String getExtension(File file) {
        var extension = "";
        String[] split = file.getName().split("\\.");
        if (split.length > 1) {
            extension = split[split.length - 1];
        }
        return extension;
    }

    public LanguageStrategy createStrategyForFile() {
        return switch (getExtension(file()).toLowerCase()) {
            case "c", "cpp", "cc" -> new CppStrategy();
            case "py" -> new PyStrategy();
            case "java" -> new JavaStrategy();
            case "cs" -> new CSharpStrategy();
            default -> null;
        };
    }

}