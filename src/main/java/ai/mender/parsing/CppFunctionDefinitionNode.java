package ai.mender.parsing;

import ai.mender.Language;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.cpp14.CPP14ParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class CppFunctionDefinitionNode
        implements FunctionDefinitionNode<CPP14Parser.FunctionDefinitionContext> {
    private final CPP14Parser.FunctionDefinitionContext ctx;

    public CppFunctionDefinitionNode(CPP14Parser.FunctionDefinitionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public CPP14Parser.FunctionDefinitionContext getAntlrNode() {
        return ctx;
    }

    @Override
    public String getName() {
        FunctionNameListener listener = new FunctionNameListener();
        ParseTreeWalker.DEFAULT.walk(listener, ctx);
        return Language.getTextIncludingWhitespace(listener.firstFoundId);
    }

    private static class FunctionNameListener extends CPP14ParserBaseListener {
        CPP14Parser.DeclaratoridContext firstFoundId = null;

        @Override
        public void enterDeclaratorid(CPP14Parser.DeclaratoridContext ctx) {
            if (null == firstFoundId) {
                firstFoundId = ctx;
            }
        }
    }
}
