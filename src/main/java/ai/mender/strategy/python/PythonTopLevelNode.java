package ai.mender.strategy.python;

import ai.mender.strategy.FunctionDefinitionNode;
import ai.mender.strategy.TopLevelNode;
import antlrgen.python.PythonParser;
import antlrgen.python.PythonParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.function.Consumer;

public class PythonTopLevelNode implements TopLevelNode {
    private final PythonParser.File_inputContext tree;

    public PythonTopLevelNode(PythonParser.File_inputContext tree) {
        this.tree = tree;
    }

    @Override
    public void forEachFunctionNode(Consumer<FunctionDefinitionNode> consumer) {
        PythonParserBaseListener listener =
                new PythonParserBaseListener() {
                    @Override
                    public void enterFuncdef(PythonParser.FuncdefContext ctx) {
                        consumer.accept(new PythonFunctionDefinitionNode(ctx));
                    }
                };
        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }

}
