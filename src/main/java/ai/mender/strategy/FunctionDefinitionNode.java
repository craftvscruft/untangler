package ai.mender.strategy;

import ai.mender.domain.FunctionRec;
import ai.mender.domain.SourcePosition;
import ai.mender.domain.SourceRange;
import org.antlr.v4.runtime.ParserRuleContext;

public interface FunctionDefinitionNode<T extends ParserRuleContext> {
    default int getStartLine()  {
        return getAntlrNode().start.getLine();
    }

    default FunctionRec toFunctionRec() {
        return new FunctionRec(getName(), getStartLine());
    }

    String getName();

    T getAntlrNode();

    default SourcePosition getStart() {
        var antlrNode = getAntlrNode();
        return new SourcePosition(antlrNode.start.getLine(), antlrNode.start.getCharPositionInLine());
    }

    default SourcePosition getEnd() {
        var antlrNode = getAntlrNode();
        return new SourcePosition(antlrNode.stop.getLine(), antlrNode.stop.getCharPositionInLine());
    }

    SourceRange getNameRange();

//    default SourcePosition getNameStart() {
//        var antlrNode = getNameAntlrNode();
//        if (antlrNode == null) {
//            Token token = getNameAntlrTerminal().getSymbol();
//            return new SourcePosition(token.getLine(), token.getCharPositionInLine());
//        }
//        return new SourcePosition(antlrNode.start.getLine(), antlrNode.start.getCharPositionInLine());
//    }
//
//
//
//    default SourcePosition getNameEnd() {
//        var antlrNode = getNameAntlrNode();
//
//        if (antlrNode == null) {
//            Token token = getNameAntlrTerminal().getSymbol();
//            int length = token.getStopIndex() - token.getStartIndex();
//            return new SourcePosition(token.getLine(), token.getCharPositionInLine() + length);
//        }
//        int charPositionInLine = antlrNode.stop.getCharPositionInLine();
//        return new SourcePosition(antlrNode.stop.getLine(), charPositionInLine);
//    }

}
