package ai.mender.strategy.python;

import ai.mender.domain.FunctionRec;
import ai.mender.strategy.TopLevelNode;
import antlrgen.python.PythonParser;
import antlrgen.python.PythonParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;

public class PythonTopLevelNode implements TopLevelNode {
    private final PythonParser.File_inputContext tree;

    public PythonTopLevelNode(PythonParser.File_inputContext tree) {
        this.tree = tree;
    }

    @Override
    public void collectFunctions(List<FunctionRec> items) {
        PythonParserBaseListener listener =
                new PythonParserBaseListener() {

                    @Override
                    public void enterFuncdef(PythonParser.FuncdefContext ctx) {
                        items.add(new PythonFunctionDefinitionNode(ctx).toFunctionRec());
                    }
                };
        ParseTreeWalker.DEFAULT.walk(listener, tree);
    }
}
