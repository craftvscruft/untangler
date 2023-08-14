package ai.mender.strategy.java;

import ai.mender.domain.FunctionRec;
import ai.mender.strategy.TopLevelNode;
import antlrgen.java20.Java20Parser;
import antlrgen.java20.Java20ParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;

public class JavaTopLevelNode implements TopLevelNode {
    private final Java20Parser.CompilationUnitContext tree;

    public JavaTopLevelNode(Java20Parser.CompilationUnitContext tree) {
        this.tree = tree;
    }

    @Override
    public void collectFunctions(List<FunctionRec> items) {
        Java20ParserBaseListener listener =
                new Java20ParserBaseListener() {

                    @Override
                    public void enterMethodDeclaration(Java20Parser.MethodDeclarationContext ctx) {
                        items.add(new JavaFunctionDefinitionNode(ctx).toFunctionRec());
                    }
                };

        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }
}
