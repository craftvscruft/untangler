package ai.mender;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;

import java.io.IOException;

public class JsonPrettyPrinter
    implements PrettyPrinter, java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    protected String _rootValueSeparator;

    protected Separators _separators;

    public JsonPrettyPrinter() {
        this(DEFAULT_ROOT_VALUE_SEPARATOR.toString());
    }

    public JsonPrettyPrinter(String rootValueSeparator) {
        _rootValueSeparator = rootValueSeparator;
        _separators = DEFAULT_SEPARATORS;
    }

    public void setRootValueSeparator(String sep) {
        _rootValueSeparator = sep;
    }


    public JsonPrettyPrinter setSeparators(Separators separators) {
        _separators = separators;
        return this;
    }


    @Override
    public void writeRootValueSeparator(JsonGenerator g) throws IOException
    {
        if (_rootValueSeparator != null) {
            g.writeRaw(_rootValueSeparator);
        }
    }

    @Override
    public void writeStartObject(JsonGenerator g) throws IOException
    {
        g.writeRaw('{');
    }
    
    @Override
    public void beforeObjectEntries(JsonGenerator g) throws IOException
    {
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException
    {
        g.writeRaw(_separators.getObjectFieldValueSeparator());
        g.writeRaw(' ');
    }

    @Override
    public void writeObjectEntrySeparator(JsonGenerator g) throws IOException
    {
        g.writeRaw(_separators.getObjectEntrySeparator());
    }

    @Override
    public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException
    {
        g.writeRaw('}');
    }
    
    @Override
    public void writeStartArray(JsonGenerator g) throws IOException
    {
        g.writeRaw('[');
        g.writeRaw("\n  ");
    }
    
    @Override
    public void beforeArrayValues(JsonGenerator g) throws IOException
    {

    }

    @Override
    public void writeArrayValueSeparator(JsonGenerator g) throws IOException
    {
        g.writeRaw(_separators.getArrayValueSeparator());
        g.writeRaw("\n  ");
    }
    
    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException
    {
        g.writeRaw('\n');
        g.writeRaw(']');
    }
}
