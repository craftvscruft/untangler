package ai.mender.untangler.clang;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;

public record ClangParseConfig(String filePath) {
    public boolean matchesFilePath(String otherPath) {
        return Path.of(filePath()).equals(Path.of(otherPath));
    }
}