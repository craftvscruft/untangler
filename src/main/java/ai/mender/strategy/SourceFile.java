package ai.mender.strategy;

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

    public void a() {
        return;
    }
}