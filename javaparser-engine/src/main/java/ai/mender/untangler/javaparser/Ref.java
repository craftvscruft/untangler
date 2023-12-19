package ai.mender.untangler.javaparser;

import ai.mender.untangler.shared.SimpleSelector;
import ai.mender.untangler.shared.response.Reference;
import ai.mender.untangler.shared.response.SourceRange;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import java.util.Set;

public class Ref {
    static Node resolveDeclNameNode(Node declNode, String name) {
//        System.out.println(declNode.getClass());
        if (declNode instanceof CallableDeclaration<?> callDeclNode) {
            return callDeclNode.getName();
        }
        if (declNode instanceof TypeDeclaration<?> typeDeclNode) {
            return typeDeclNode.getName();
        }
        if (declNode instanceof Parameter paramNode) {
            return paramNode.getName();
        }
        if (declNode instanceof VariableDeclarator varDeclNode) {
            return varDeclNode.getName();
        }
        if (declNode instanceof VariableDeclarationExpr varDeclExprNode) {
            for (var varDeclNode : varDeclExprNode.getVariables()) {
                if (varDeclNode.getName().asString().equals(name)) {
                    return varDeclNode.getName();
                }
            }
        }
        if (declNode instanceof NodeWithSimpleName nodeWithSimpleName) {
            return nodeWithSimpleName.getName();
        }
        return declNode;
    }

    static void addMethodReferenceExprToReferenceResponse(JavaParserFacade parserFacade, SimpleSelector selector, MethodReferenceExpr node, Set<Reference> references, Set<SourceRange> declarations) {
        String name = node.toString();
        if (selector.matchesName(name)) {
            SourceRange sourceRange = JavaParserEngine.toSourceRange(node);
            if (selector.matchesLineRange(sourceRange)) {
                var ref = parserFacade.solve(node);
                // Seems like this could fail silently, should we handle?
                (ref).getDeclaration().ifPresent(decl -> {
                    decl.toAst().ifPresent(declNode -> {
                        SourceRange declSourceRange = JavaParserEngine.toSourceRange(resolveDeclNameNode(declNode, name));
                        references.add(new Reference(name, sourceRange, declSourceRange));
                        declarations.add(declSourceRange);
                    });
                });
            }
        }
    }
}
