package ai.mender.untangler.shared;

import ai.mender.untangler.shared.ISourceFile;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

public class StringSourceFile implements ISourceFile {
    private String name;
    private String source;

    public StringSourceFile(String name, String source) {
        this.name = name;

        this.source = source;
    }


    @Override
    public Reader getReader() {
        return new StringReader(source);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Charset getCharset() {
        return Charset.defaultCharset();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
