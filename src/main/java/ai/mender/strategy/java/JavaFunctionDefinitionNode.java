package ai.mender.strategy.java;

import ai.mender.parsing.SyntaxTreeUtil;
import ai.mender.strategy.FunctionDefinitionNode;
import ai.mender.untangler.shared.response.SourcePosition;
import ai.mender.untangler.shared.response.SourceRange;
import antlrgen.java20.Java20Parser;
import org.antlr.v4.runtime.tree.TerminalNode;


public class JavaFunctionDefinitionNode implements FunctionDefinitionNode<Java20Parser.MethodDeclarationContext> {
    private final Java20Parser.MethodDeclarationContext ctx;

    public JavaFunctionDefinitionNode(Java20Parser.MethodDeclarationContext ctx) {
        this.ctx = ctx;
    }


    private TerminalNode getNameAntlrTerminal() {
        Java20Parser.MethodDeclaratorContext methodDeclarator = ctx.methodHeader().methodDeclarator();
        return methodDeclarator.Identifier();
    }

    @Override
    public String getName() {
        return SyntaxTreeUtil.getTextIncludingWhitespace(getNameAntlrTerminal());
    }

    @Override
    public Java20Parser.MethodDeclarationContext getAntlrNode() {
        return ctx;
    }

    @Override
    public SourceRange getNameRange() {
        TerminalNode terminal = getNameAntlrTerminal();
        int startColumn = terminal.getSymbol().getCharPositionInLine();
        int line = terminal.getSymbol().getLine();
        int length = terminal.getSymbol().getText().length();
        SourcePosition start = new SourcePosition(line, startColumn);
        SourcePosition end = new SourcePosition(line, startColumn + length);
        return new SourceRange(start, end);
    }
}
