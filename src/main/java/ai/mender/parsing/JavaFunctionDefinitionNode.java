package ai.mender.parsing;

import ai.mender.Language;
import antlrgen.java20.Java20Parser;


public class JavaFunctionDefinitionNode implements FunctionDefinitionNode<Java20Parser.MethodDeclarationContext> {
    private final Java20Parser.MethodDeclarationContext ctx;

    public JavaFunctionDefinitionNode(Java20Parser.MethodDeclarationContext ctx) {
        this.ctx = ctx;
    }


    @Override
    public String getName() {
        return Language.getTextIncludingWhitespace(ctx.methodHeader().methodDeclarator().Identifier());
    }

    @Override
    public Java20Parser.MethodDeclarationContext getAntlrNode() {
        return ctx;
    }
}
