package ai.mender.parsing;

import antlrgen.csharp.CSharpParser;

public class CSharpFunctionDefinitionNode implements FunctionDefinitionNode<CSharpParser.Method_declarationContext> {
    private final CSharpParser.Method_declarationContext ctx;

    public CSharpFunctionDefinitionNode(CSharpParser.Method_declarationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String getName() {
        return SyntaxTreeUtil.getTextIncludingWhitespace(ctx.method_member_name());
    }

    @Override
    public CSharpParser.Method_declarationContext getAntlrNode() {
        return ctx;
    }
}
