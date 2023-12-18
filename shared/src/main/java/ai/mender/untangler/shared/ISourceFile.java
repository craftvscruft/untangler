package ai.mender.untangler.shared;

import java.io.Reader;
import java.nio.charset.Charset;

public interface ISourceFile {
    Reader getReader();

    String getName();


    Charset getCharset();
}
