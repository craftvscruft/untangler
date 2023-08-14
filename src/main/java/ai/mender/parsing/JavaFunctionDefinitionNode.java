package ai.mender.parsing;

import ai.mender.Language;
import ai.mender.domain.FunctionRec;
import antlrgen.java20.Java20Parser;


public class JavaFunctionDefinitionNode implements FunctionDefinitionNode<Java20Parser.MethodDeclarationContext> {
    private final Java20Parser.MethodDeclarationContext ctx;

    public JavaFunctionDefinitionNode(Java20Parser.MethodDeclarationContext ctx) {
        this.ctx = ctx;
    }


    public FunctionRec toFunctionRec() {
        String name = Language.getTextIncludingWhitespace(ctx.methodHeader().methodDeclarator().Identifier());
        return new FunctionRec(name, getStartLine());
    }

    @Override
    public Java20Parser.MethodDeclarationContext getAntlrNode() {
        return ctx;
    }
}
