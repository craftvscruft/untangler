package ai.mender;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class JsonPrettyPrinter extends DefaultPrettyPrinter {
    private static final long serialVersionUID = 1L;

    {
        _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
    }

    @Override
    public DefaultPrettyPrinter createInstance() {
        return this;
    }
}
