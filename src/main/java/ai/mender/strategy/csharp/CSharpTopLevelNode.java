package ai.mender.strategy.csharp;

import ai.mender.domain.FunctionRec;
import ai.mender.strategy.TopLevelNode;
import antlrgen.csharp.CSharpParser;
import antlrgen.csharp.CSharpParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;

public class CSharpTopLevelNode implements TopLevelNode {
    private final CSharpParser.Compilation_unitContext tree;

    public CSharpTopLevelNode(CSharpParser.Compilation_unitContext tree) {
        this.tree = tree;
    }

    @Override
    public void collectFunctions(List<FunctionRec> items) {
        CSharpParserBaseListener listener =
                new CSharpParserBaseListener() {

                    @Override
                    public void enterMethod_declaration(CSharpParser.Method_declarationContext ctx) {
                        items.add(new CSharpFunctionDefinitionNode(ctx).toFunctionRec());
                    }
                };

        ParseTreeWalker.DEFAULT.walk(listener, this.tree);
    }
}
