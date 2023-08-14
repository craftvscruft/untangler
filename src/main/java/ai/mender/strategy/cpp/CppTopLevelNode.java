package ai.mender.strategy.cpp;

import ai.mender.domain.FunctionRec;
import ai.mender.strategy.TopLevelNode;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.cpp14.CPP14ParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;

public class CppTopLevelNode implements TopLevelNode {
    private final CPP14Parser.TranslationUnitContext tree;

    public CppTopLevelNode(CPP14Parser.TranslationUnitContext tree) {
        this.tree = tree;
    }

    @Override
    public void collectFunctions(List<FunctionRec> items) {
        CPP14ParserBaseListener listener =
                new CPP14ParserBaseListener() {

                    @Override
                    public void enterFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
                        items.add(new CppFunctionDefinitionNode(ctx).toFunctionRec());
                    }
                };

        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }
}
