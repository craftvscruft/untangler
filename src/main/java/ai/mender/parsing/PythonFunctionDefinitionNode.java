package ai.mender.parsing;

import ai.mender.Language;
import ai.mender.domain.FunctionRec;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.cpp14.CPP14ParserBaseListener;
import antlrgen.python.PythonParser;
import antlrgen.python.PythonParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class PythonFunctionDefinitionNode
        implements FunctionDefinitionNode<PythonParser.FuncdefContext> {
    private final PythonParser.FuncdefContext ctx;

    public PythonFunctionDefinitionNode(PythonParser.FuncdefContext ctx) {
        this.ctx = ctx;
    }

    public FunctionRec toFunctionRec() {
        return new FunctionRec(findName());
    }

    private String findName() {
        return Language.getTextIncludingWhitespace(ctx.name());
    }


}
