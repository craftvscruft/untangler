package ai.mender.strategy.cpp;

import ai.mender.domain.SourceRange;
import ai.mender.parsing.Ast;
import ai.mender.parsing.SyntaxTreeUtil;
import ai.mender.strategy.FunctionDefinitionNode;
import ai.mender.strategy.TopLevelNode;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.cpp14.CPP14ParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class CppTopLevelNode implements TopLevelNode {
    final private Logger LOG = LoggerFactory.getLogger(CppTopLevelNode.class);
    private final CPP14Parser.TranslationUnitContext tree;
    private CPP14Parser parser;

    public CppTopLevelNode(CPP14Parser.TranslationUnitContext tree) {
        this.tree = tree;
    }

    public CppTopLevelNode(CPP14Parser.TranslationUnitContext tree, CPP14Parser parser) {
        this.tree = tree;
        this.parser = parser;
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

    private String toStringTree() {
        return this.tree.toStringTree(this.parser);
    }

    @SuppressWarnings("deprecation")
    public List<String> exposeAstPasses() {
        var passes = new ArrayList<String>();
        passes.add(WordUtils.wrap(toStringTree(), 100));
        Ast ast = CppStrategy.parseAst(this);
        passes.add(SyntaxTreeUtil.astToSexpr(ast));
        Ast root = CppStrategy.getTransformedAst(ast);
        passes.add(SyntaxTreeUtil.astToSexpr(root));
        return passes;
    }

    public SourceRange sourceRange() {
        return SyntaxTreeUtil.nodeToSourceRange(this.tree);
    }

    CPP14Parser.TranslationUnitContext getTree() {
        return this.tree;
    }
}


