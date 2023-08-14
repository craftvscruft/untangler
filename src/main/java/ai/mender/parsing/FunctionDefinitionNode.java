package ai.mender.parsing;

import ai.mender.domain.FunctionRec;
import org.antlr.v4.runtime.ParserRuleContext;

public interface FunctionDefinitionNode<T extends ParserRuleContext> {
    default int getStartLine()  {
        return getAntlrNode().start.getLine();
    }

    FunctionRec toFunctionRec();

    T getAntlrNode();
}
