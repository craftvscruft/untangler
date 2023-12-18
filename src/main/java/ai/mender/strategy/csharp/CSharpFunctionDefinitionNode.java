package ai.mender.strategy.csharp;

import ai.mender.parsing.SyntaxTreeUtil;
import ai.mender.strategy.FunctionDefinitionNode;
import ai.mender.untangler.shared.response.SourceRange;
import antlrgen.csharp.CSharpParser;
import org.antlr.v4.runtime.ParserRuleContext;

public class CSharpFunctionDefinitionNode implements FunctionDefinitionNode<CSharpParser.Method_declarationContext> {
    private final CSharpParser.Method_declarationContext ctx;

    public CSharpFunctionDefinitionNode(CSharpParser.Method_declarationContext ctx) {
        this.ctx = ctx;
    }

    private ParserRuleContext getNameAntlrNode() {
        return ctx.method_member_name();
    }

    @Override
    public String getName() {
        return SyntaxTreeUtil.getTextIncludingWhitespace(getNameAntlrNode());
    }

    @Override
    public CSharpParser.Method_declarationContext getAntlrNode() {
        return ctx;
    }

    @Override
    public SourceRange getNameRange() {
        return SyntaxTreeUtil.nodeToSourceRange(getNameAntlrNode());
    }
}
