package ai.mender.parsing;

import ai.mender.Language;
import ai.mender.domain.FunctionRec;
import antlrgen.CPP14Parser;
import antlrgen.CPP14ParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class CppFunctionDefinitionNode implements FunctionDefinitionNode<CPP14Parser.FunctionDefinitionContext> {
    private final CPP14Parser.FunctionDefinitionContext ctx;

    public CppFunctionDefinitionNode(CPP14Parser.FunctionDefinitionContext ctx) {
        this.ctx = ctx;
    }

    public FunctionRec toFunctionRec() {
        return new FunctionRec(findName());
    }

    private String findName() {
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
