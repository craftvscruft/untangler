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

public record SourceFile(File file) implements ISourceFile {
    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    private static String getExtension(String name) {
        var extension = "";

        String[] split = name.split("\\.");
        if (split.length > 1) {
            extension = split[split.length - 1];
        }
        return extension;
    }

    @Override
    public CharStream getCharStream() {
        try {
            return CharStreams.fromFileName(
                    file().getAbsolutePath(), CharsetUtils.detectFileCharset(file()));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public LanguageStrategy createStrategyForFile() {
        return createStrategyForExtension(getExtension(this.file()));
    }

    private static LanguageStrategy createStrategyForExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "c", "cpp", "cc" -> new CppStrategy();
            case "py" -> new PythonStrategy();
            case "java" -> new JavaStrategy();
            case "cs" -> new CSharpStrategy();
            default -> null;
        };
    }

    public static class StringSourceFile implements ISourceFile {
        private String name;
        private String source;

        public StringSourceFile(String name, String source) {
            this.name = name;

            this.source = source;
        }

        @Override
        public CharStream getCharStream() {
            return CharStreams.fromString(source, name);
        }

        @Override
        public LanguageStrategy createStrategyForFile() {
            return createStrategyForExtension(getExtension(name));
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

}