package ai.mender.untangler.javaparser;


import ai.mender.untangler.shared.ISourceFile;
import ai.mender.untangler.shared.LanguageEngine;
import ai.mender.untangler.shared.SimpleSelector;
import ai.mender.untangler.shared.response.*;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;


import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;


public class JavaParserEngine implements LanguageEngine {

    // When we resolve across files, we'll have a value to put here.
    private String srcDir = System.getProperty("java.io.tmpdir");;

    {
        // If other people on the classpath use StaticJavaParser, we can switch to a local instance
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
    }

    @Override
    public Stream<FunctionRec> functions(ISourceFile sourceFile) {
        CompilationUnit compilationUnit = parseFile(sourceFile);
        return compilationUnit.findAll(MethodDeclaration.class).stream().map((MethodDeclaration node) ->
                new FunctionRec(node.getName().getIdentifier(), toSourceRange(node))
        );
    }

    private static CompilationUnit parseFile(ISourceFile sourceFile) {
        return StaticJavaParser.parse(sourceFile.getReader());
    }

    @Override
    public Stream<CommentRec> comments(ISourceFile sourceFile) {
        CompilationUnit compilationUnit = parseFile(sourceFile);
        return compilationUnit.getAllComments().stream().map((Comment node) ->
                new CommentRec(toSourceRange(node), node.toString()));
    }

    static SourceRange toSourceRange(Node node) {
        Position begin = node.getRange().get().begin;
        Position end = node.getRange().get().end;
        SourcePosition startPos = new SourcePosition(begin.line, begin.column);
        SourcePosition endPos = new SourcePosition(end.line, end.column + 1);
        return new SourceRange(startPos, endPos);
    }

    @Override
    public String formatMultiLineComment(String text) {
        return String.format("/* %s */\n", text);
    }

    @Override
    public ReferencesResponse references(ISourceFile sourceFile, SimpleSelector selector) {
        CompilationUnit compilationUnit = parseFile(sourceFile);
        var combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new JavaParserTypeSolver(srcDir));
        JavaParserFacade parserFacade = JavaParserFacade.get(combinedTypeSolver);

        Set<Reference> references = new TreeSet<>();
        Set<SourceRange> declarations = new TreeSet<>();
        compilationUnit.findAll(SimpleName.class).stream().forEach(node ->
                addSimpleNameToReferenceResponse(parserFacade, selector, node, references, declarations)
        );
        compilationUnit.findAll(MethodReferenceExpr.class).stream().forEach(node ->
                Ref.addMethodReferenceExprToReferenceResponse(parserFacade, selector, node, references, declarations)
        );
        return new ReferencesResponse(selector, declarations.stream().toList(), references.stream().toList());
    }

    private static void addSimpleNameToReferenceResponse(JavaParserFacade parserFacade, SimpleSelector selector, SimpleName node, Set<Reference> references, Set<SourceRange> declarations) {
        String name = node.toString();
        if (selector.matchesName(name)) {
            SourceRange sourceRange = toSourceRange(node);
            if (selector.matchesLineRange(sourceRange)) {
//                parserFacade.getSymbolSolver()
                try {
                    var ref = parserFacade.solve(node);

                    ref.getDeclaration()
                            .flatMap((decl -> decl.toAst()))
                            .ifPresent(declNode -> {
                                SourceRange declSourceRange = toSourceRange(Ref.resolveDeclNameNode(declNode, name));
                                if (!sourceRange.equals(declSourceRange)) {
                                    references.add(new Reference(name, sourceRange, declSourceRange));
                                }
                                declarations.add(declSourceRange);
                            });
                } catch (UnsolvedSymbolException e) {
                    // should these be added to the result as Unknown?
                }

            }
        }
    }

}
