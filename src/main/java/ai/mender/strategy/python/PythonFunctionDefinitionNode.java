package ai.mender.strategy.python;

import ai.mender.parsing.SyntaxTreeUtil;
import ai.mender.strategy.FunctionDefinitionNode;
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
        return SyntaxTreeUtil.getTextIncludingWhitespace(ctx.name());
    }

}
