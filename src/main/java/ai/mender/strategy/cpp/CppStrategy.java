package ai.mender.strategy.cpp;

import ai.mender.domain.ReferencesResponse;
import ai.mender.parsing.*;
import ai.mender.strategy.*;
import antlrgen.cpp14.CPP14Lexer;
import antlrgen.cpp14.CPP14Parser;
import antlrgen.cpp14.CPP14ParserBaseListener;
import com.google.common.annotations.VisibleForTesting;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.function.Consumers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class CppStrategy implements LanguageStrategy {
    final Logger LOG = LoggerFactory.getLogger(CppStrategy.class);

    @VisibleForTesting
    public static Ast getTransformedAst(Ast ast) {
        AstTransformer transformer = new AstTransformer();
        transformer.registerTrackingListener("fn");
        transformer.registerTrackingListener("fnBody");
        transformer.getListenerRegistry().listenForTag("idExpr",
                node -> {
                    if (transformer.hasAncestorWithTag("fn") &&
                            !transformer.parentTagsStoppingAt("fn").contains("fnBody")) {

                        node.children().forEach(child -> {
                            transformer.treeBuilder.setCurrentRange(child.range());
                            transformer.treeBuilder.addFirstOrAccumulate("name", "params", child.tag());
                        });
                        transformer.treeBuilder.setCurrentRange(node.range());

                    } else if (transformer.hasAncestorWithTag("simpleDeclWithSpec") &&
                            !transformer.hasAncestorWithTag("initializer")) {
                        node.children().forEach(child -> {
                                transformer.treeBuilder.setCurrentRange(child.range());
                                transformer.treeBuilder.accumulate("decl", child.tag());
                        });
                        transformer.treeBuilder.setCurrentRange(node.range());
                    } else {
                        node.children().forEach(child -> {
                            transformer.treeBuilder.setCurrentRange(child.range());
                            transformer.treeBuilder.accumulate("ref", child.tag());
                        });
                        transformer.treeBuilder.setCurrentRange(node.range());
                    }
                }, node -> {});


        return transformer.transform(ast);
    }

    @VisibleForTesting
    public static Ast parseAst(CppTopLevelNode cppTopLevelNode) {
        TreeBuilder treeBuilder = new TreeBuilder();
        treeBuilder.setCurrentRange(cppTopLevelNode.sourceRange());
        HeterogeneousContainer listenerRegistry = new HeterogeneousContainer();
        listenerRegistry.register(CPP14Parser.FunctionDefinitionContext.class,
                ctx -> treeBuilder.enter("fn"),
                ctx -> treeBuilder.exit());
        listenerRegistry.register(CPP14Parser.FunctionBodyContext.class,
                ctx -> treeBuilder.enter("fnBody"),
                ctx -> treeBuilder.exit());
        listenerRegistry.register(CPP14Parser.InitializerContext.class,
                ctx -> treeBuilder.enter("initializer"),
                ctx -> treeBuilder.exit());
        listenerRegistry.register(CPP14Parser.SimpleDeclarationContext.class,
                ctx -> treeBuilder.enter(ctx.declSpecifierSeq() == null ? "simpleDecl" : "simpleDeclWithSpec"),
                ctx -> treeBuilder.exit());
        listenerRegistry.register(CPP14Parser.IdExpressionContext.class,
                ctx -> treeBuilder.accumulate("idExpr", ctx.getText()),
                Consumers.nop());

        var listenerNew = new CPP14ParserBaseListener() {
            @Override
            public void enterEveryRule(ParserRuleContext ctx) {
                treeBuilder.setCurrentRange(SyntaxTreeUtil.nodeToSourceRange(ctx));
                listenerRegistry.acceptEnter(ctx);
            }

            @Override
            public void exitEveryRule(ParserRuleContext ctx) {
                listenerRegistry.acceptExit(ctx);
            }
        };
        ParseTreeWalker.DEFAULT.walk(listenerNew, cppTopLevelNode.getTree());


        return treeBuilder.root();
    }

    @Override
    public CppTopLevelNode parseTopLevel(ISourceFile sourceFile) {
        boolean throwOnParseError = false;
        CPP14Lexer lexer = new CPP14Lexer(sourceFile.getCharStream());
        if (throwOnParseError) {
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        }

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(commonTokenStream);
        if (throwOnParseError) {
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        }
        CPP14Parser.TranslationUnitContext tree =
                parser.translationUnit();
        return new CppTopLevelNode(tree, parser);
    }

    public ReferencesResponse references(TopLevelNode root, String nameToResolve) {
//        LOG.debug("References");

        CppTopLevelNode root1 = (CppTopLevelNode) root;
        Ast tree = getTransformedAst(parseAst(root1));
        Scope scope = new Scope(nameToResolve);

        AstWalker astWalker = new AstWalker();
//        astWalker
        /* Example:

(root
  (decl global1)
  (fn
    (name foo)
    (params arg1 arg1)
    (fnBody
      (decl s)
      (ref currentPoint calculateBezierPoint t calculateBezierPoint u global1 arg1))))
         */
        astWalker.getListenerRegistry().listenForTag("decl",
                (node)-> node.children()
                        .forEach(scope::addDeclaration),
                (node)->{});
        astWalker.getListenerRegistry().listenForTag("params",
                (node)-> node.children()
                        .forEach(scope::addDeclaration),
                (node)->{});
        astWalker.getListenerRegistry().listenForTag("fn",
                (fnNode)-> {
                    Optional<Ast> maybeNameNode = fnNode.getFirstChildByTag("name");
                    // Define fn name in the scope
                    maybeNameNode.ifPresent(nameNode -> {
                        List<String> nameTags = nameNode.childTags();
                        if (!nameTags.isEmpty()) {
                            // Warn if size not 1?
                            scope.addDeclaration(nameNode.children().get(0));
                        }
                    });
                    scope.push();
                },
                (node)-> scope.pop());
        astWalker.getListenerRegistry().listenForTag("ref",
                (node)-> node.children()
                        .forEach(refNode -> scope.addRef(refNode)),
                (node)->{});
        astWalker.walk(tree);
        scope.resolveUnknownFromCurrentScope();
        return scope.getReferences();
    }
}
