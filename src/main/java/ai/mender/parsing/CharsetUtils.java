package ai.mender.parsing;

import com.google.common.io.ByteSource;
import org.mozilla.universalchardet.UniversalDetector;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class CharsetUtils {
    public static Charset detectFileCharset(File file) throws IOException {
        UniversalDetector universalDetector = new UniversalDetector(null);
        int maxToRead = 4096;
        ByteSource byteSource = com.google.common.io.Files.asByteSource(file).slice(0, maxToRead);
        byte[] bytes = byteSource.read();
        universalDetector.handleData(bytes, 0, bytes.length);
        universalDetector.dataEnd();
        return Charset.forName(universalDetector.getDetectedCharset());
    }
}
