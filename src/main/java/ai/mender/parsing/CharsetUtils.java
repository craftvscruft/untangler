package ai.mender.parsing;

import com.ibm.icu.text.CharsetDetector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class CharsetUtils {
    public static Charset detectFileCharset(File file) throws IOException {
        CharsetDetector charsetDetector = new CharsetDetector();
        try (var inputStream = Files.newInputStream(Path.of(file.getAbsolutePath()))) {
            var inputStreamIgnoringRestart = new InputStream() {

                @Override
                public int read() throws IOException {
                    return inputStream.read();
                }

                @Override
                public void mark(int readLimit) {
                    // ignore
                }

                @Override
                public void reset() {
                    // ignore
                }
            };
            charsetDetector.setText(inputStreamIgnoringRestart);

        }
        return Charset.forName(charsetDetector.detect().getName());
    }
}
