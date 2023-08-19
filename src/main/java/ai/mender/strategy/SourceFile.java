package ai.mender.strategy;

import ai.mender.domain.SourceEdit;
import ai.mender.parsing.CharsetUtils;
import ai.mender.strategy.cpp.CppStrategy;
import ai.mender.strategy.csharp.CSharpStrategy;
import ai.mender.strategy.java.JavaStrategy;
import ai.mender.strategy.python.PythonStrategy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

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

    public static ImmutableList<SourceEdit> sortedEditsCheckedForDupes(List<SourceEdit> inputEdits) {
        var ordering = Ordering.from(Comparator.comparing((SourceEdit e) -> e.start()));
        var sortedEdits = ordering.immutableSortedCopy(inputEdits);
        for (int i = 0; i < sortedEdits.size() - 1; i++) {
            var edit1 = sortedEdits.get(i);
            var edit2 = sortedEdits.get(i+1);
            if (edit1.end().compareTo(edit2.start()) > 0) {
                throw new RuntimeException(String.format("Edit ranges cannot overlap: %s <-> %s", edit1, edit2));
            }
        }
        return sortedEdits;
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

    public void update(File file, List<SourceEdit> inputEdits, PrintWriter consoleOut) throws IOException {
        List<SourceEdit> edits = SourceFile.sortedEditsCheckedForDupes(inputEdits);
        Path backupPath = backupFile(file, consoleOut);
        Charset charset = CharsetUtils.detectFileCharset(file);
        consoleOut.println("Writing " + file);
        try (var in = new FileInputStream(backupPath.toFile())) {
            try (var out = new FileOutputStream(file())) {
                Reader reader = new BufferedReader(new InputStreamReader(in, charset));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                applyEdits(edits, reader, writer);
                reader.close();
                writer.close();
            }
        } catch (Exception e) {
            consoleOut.println("Something went wrong, attempting to restore from backup");
            try {
                Files.move(backupPath, file.toPath());
            } catch (Exception e2) {
                consoleOut.println(e2.getMessage());
            }
            throw e;
        }
    }

    private static Path backupFile(File file, PrintWriter consoleOut) throws IOException {
        Path backupPath = Path.of(file.toPath().toAbsolutePath() + ".bak");
        consoleOut.println("Saving backup to " + backupPath);
        Files.deleteIfExists(backupPath);
        return Files.copy(file.toPath(), backupPath);
    }



    public static void applyEdits(List<SourceEdit> edits, Reader reader, Writer writer) throws IOException {
        // Edits must be in order of ascending start and cannot overlap.
        int line = 1;
        int col = 1;
        int readCh;
        for (SourceEdit edit : edits) {
            // Loop until break, due to edit out of range or end of input
            for(;;) {
                boolean isAtEditStart = edit.start().line() == line && edit.start().col() == col;
                if (isAtEditStart) {
                    writer.write(edit.text());
                    if (edit.start().equals(edit.end())) {
                        break; // Don't consume, just move on to next edit.
                    }
                }

                readCh = reader.read();
                if (readCh == -1) {
                    break;
                }

                boolean gteStart = line > edit.start().line() ||
                        (edit.start().line() == line && col >= edit.start().col());
                boolean ltEnd = line < edit.end().line() ||
                        (edit.end().line() == line && col < edit.end().col());
                boolean isWithinEditReplaceRange = gteStart && ltEnd;
                if (!isWithinEditReplaceRange) {
                    writer.write((char) readCh);
                }
                col++;
                if (readCh == '\n') {
                    // Will cover both "\r\n" and "\n" - enough, right?
                    line++;
                    col = 1;
                }
                boolean isAfterEditEnd = line > edit.end().line() || (line == edit.end().line() && col > edit.end().col());
                if (isAfterEditEnd) {
                    // After edit range, we're done.
                    // Did we never write it? This could be an error case.
                    break;
                }
            }
        }
        // Copy Remaining
        while ((readCh = reader.read()) != -1) {
            writer.write((char) readCh);
        }
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