package ai.mender.strategy.cpp;

import ai.mender.parsing.SyntaxTreeUtil;
import ai.mender.strategy.FunctionDefinitionNode;
import ai.mender.untangler.shared.response.SourceRange;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.cpp14.CPP14ParserBaseListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class CppFunctionDefinitionNode
        implements FunctionDefinitionNode<CPP14Parser.FunctionDefinitionContext> {
    private final CPP14Parser.FunctionDefinitionContext ctx;

    public CppFunctionDefinitionNode(CPP14Parser.FunctionDefinitionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String getName() {
        return SyntaxTreeUtil.getTextIncludingWhitespace(getNameAntlrNode());
    }

    @Override
    public CPP14Parser.FunctionDefinitionContext getAntlrNode() {
        return ctx;
    }

    @Override
    public SourceRange getNameRange() {
        return SyntaxTreeUtil.nodeToSourceRange(getNameAntlrNode());
    }

    private ParserRuleContext getNameAntlrNode() {
        FunctionNameListener listener = new FunctionNameListener();
        ParseTreeWalker.DEFAULT.walk(listener, ctx);
        return listener.firstFoundId;
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
