package ai.mender.strategy;

public class LanguageStrategies {
    public static LanguageStrategy createStrategyForFile(SourceFile sourceFile) {
        return switch (SourceFile.getExtension(sourceFile.file()).toLowerCase()) {
            case "c", "cpp", "cc" -> new CppStrategy();
            case "py" -> new PyStrategy();
            case "java" -> new JavaStrategy();
            case "cs" -> new CSharpStrategy();
            default -> null;
        };
    }

}
