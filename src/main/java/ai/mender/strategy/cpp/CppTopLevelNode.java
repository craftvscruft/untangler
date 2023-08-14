package ai.mender.strategy.cpp;

import ai.mender.strategy.FunctionDefinitionNode;
import ai.mender.strategy.TopLevelNode;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.cpp14.CPP14ParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.function.Consumer;

public class CppTopLevelNode implements TopLevelNode {
    private final CPP14Parser.TranslationUnitContext tree;

    public CppTopLevelNode(CPP14Parser.TranslationUnitContext tree) {
        this.tree = tree;
    }

    @Override
    public void forEachFunctionNode(Consumer<FunctionDefinitionNode> consumer) {
        CPP14ParserBaseListener listener =
                new CPP14ParserBaseListener() {

                    @Override
                    public void enterFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
                        consumer.accept(new CppFunctionDefinitionNode(ctx));
                    }
                };

        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }


}
