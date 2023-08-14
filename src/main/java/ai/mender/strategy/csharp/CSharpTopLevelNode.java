package ai.mender.strategy.csharp;

import ai.mender.strategy.FunctionDefinitionNode;
import ai.mender.strategy.TopLevelNode;
import antlrgen.csharp.CSharpParser;
import antlrgen.csharp.CSharpParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.function.Consumer;

public class CSharpTopLevelNode implements TopLevelNode {
    private final CSharpParser.Compilation_unitContext tree;

    public CSharpTopLevelNode(CSharpParser.Compilation_unitContext tree) {
        this.tree = tree;
    }

    @Override
    public void forEachFunctionNode(Consumer<FunctionDefinitionNode> consumer) {
        CSharpParserBaseListener listener =
            new CSharpParserBaseListener() {

                @Override
                public void enterMethod_declaration(CSharpParser.Method_declarationContext ctx) {
                    consumer.accept(new CSharpFunctionDefinitionNode(ctx));
                }
            };

        ParseTreeWalker.DEFAULT.walk(listener, this.tree);
    }
}
