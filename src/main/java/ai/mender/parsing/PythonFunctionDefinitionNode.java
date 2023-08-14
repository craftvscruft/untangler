package ai.mender.parsing;

import ai.mender.Language;
import antlrgen.python.PythonParser;

public class PythonFunctionDefinitionNode
        implements FunctionDefinitionNode<PythonParser.FuncdefContext> {
    private final PythonParser.FuncdefContext ctx;

    public PythonFunctionDefinitionNode(PythonParser.FuncdefContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public PythonParser.FuncdefContext getAntlrNode() {
        return ctx;
    }

    @Override
    public String getName() {
        return Language.getTextIncludingWhitespace(ctx.name());
    }

}
