package ai.strategy;

import ai.mender.strategy.CppStrategy;
import ai.mender.strategy.LanguageStrategy;
import ai.mender.strategy.PyStrategy;

import java.io.File;

public class LanguageStrategies {
    public static LanguageStrategy createStrategyForFile(File file1) {
        return switch (getExtension(file1).toLowerCase()) {
            case "c", "cpp", "cc" -> new CppStrategy();
            case "py" -> new PyStrategy();
            default -> null;
        };
    }

    private static String getExtension(File file) {
        var extension = "";
        String[] split = file.getName().split("\\.");
        if (split.length > 1) {
            extension = split[split.length - 1];
        }
        return extension;
    }
}
