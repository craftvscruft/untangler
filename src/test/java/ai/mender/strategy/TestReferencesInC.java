package ai.mender.strategy;

import ai.mender.SimpleSelector;
import ai.mender.domain.Reference;
import ai.mender.domain.ReferencesResponse;
import ai.mender.untangler.shared.SourceRange;
import ai.mender.strategy.cpp.CppStrategy;
import ai.mender.strategy.cpp.CppTopLevelNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


public class TestReferencesInC {
    @Test
    public void referenceInAnotherFunction() {
        String code = """
                int foo() {
                }
                int bar() {
                    foo();
                }""";
        var refResponse = parseRefs(code, "foo");
        Assertions.assertEquals(1, refResponse.references().size(), refResponse.toString());
        Assertions.assertEquals(1, refResponse.declarations().size(), refResponse.toString());
        Reference reference = refResponse.references().get(0);
        SourceRange declaration = refResponse.declarations().get(0);
        Assertions.assertEquals(4, reference.range().start().line());
        Assertions.assertEquals(1, reference.declarationRange().start().line());
        Assertions.assertEquals(1, declaration.start().line());
    }

    @Test
    public void referenceInAnotherFunctionAreCounted() {
        String code = """
                int foo() {
                }
                int bar() {
                    foo();
                    bar();
                }""";
        var refs = parseRefs(code, "foo");
        Assertions.assertEquals(1, refs.references().size(), refs.toString());
        Assertions.assertEquals(1, refs.declarations().size(), refs.toString());
    }

    @Test
    public void referenceInAnotherFunctionDeclaredLaterAreCounted() {
        String code = """
                int bar() {
                    foo();
                    bar();
                }
                int foo() { /* Line 5*/
                }""";
        var refs = parseRefs(code, "foo");
        List<Reference> references = refs.references();
        List<SourceRange> declarations = refs.declarations();

        Assertions.assertEquals(1, declarations.size(), refs.toString());
        Assertions.assertEquals(1, references.size(), refs.toString());
        Reference reference = references.get(0);
        Assertions.assertEquals(2, reference.range().start().line(), refs.toString());
        Assertions.assertEquals(5, reference.declarationRange().start().line(), refs.toString());
    }


    @Test
    public void doNotCountShadowedReferences() {
        String code = """
                int foo() {
                }
                int bar() {
                    float foo; /* Line 4 */
                    foo();     /* Line 5 */
                }""";
        var refResponse = parseRefs(code, "foo");
        List<Reference> references = refResponse.references();
        Assertions.assertEquals(1, references.size(), refResponse.toString());
        Assertions.assertEquals(5, references.get(0).range().start().line());
        Assertions.assertEquals(4, references.get(0).declarationRange().start().line());
    }

    private static ReferencesResponse parseRefs(String code, String name) {
        CppStrategy strategy = new CppStrategy();
        SourceFile.StringSourceFile stringSourceFile = new SourceFile.StringSourceFile("main.c", code);
        CppTopLevelNode topLevelNode = strategy.parseTopLevel(stringSourceFile);
        return strategy.references(topLevelNode, SimpleSelector.parse(name));
    }

    @Test
    public void functionDefinitionsWithNoBodyCountAsRefsNotDeclarations() {
        String code = """
                int foo();
                
                int foo() {
                }
        """;
        var refResponse = parseRefs(code, "foo");
        Assertions.assertEquals(1, refResponse.references().size(), refResponse.toString());
        Assertions.assertEquals(1, refResponse.declarations().size(), refResponse.toString());
        Reference reference = refResponse.references().get(0);
        SourceRange declaration = refResponse.declarations().get(0);
        Assertions.assertEquals(1, reference.range().start().line());
        Assertions.assertEquals(3, reference.declarationRange().start().line());
        Assertions.assertEquals(3, declaration.start().line());
    }

    @Test
    public void regressionFunctionCall() {
        // These function calls are getting parsed as though the function name is a class name.
        // This is addressed by treating type names as references.
        // Snippet of parse tree:
        // (simpleDeclaration (declSpecifierSeq (declSpecifier (typeSpecifier
        //    (trailingTypeSpecifier (simpleTypeSpecifier (theTypeName (className applesauce4))))))) ... )
        String code = """
                    void processInputWithTiming(int inputChar, clock_t startTime) {
                        for (; iteration < 200 + lineCount * 25; iteration++) {
                            applesauce4(inputChar);
                            applesauce3(startTime);
                        }
                    }
                """;

        var refResponse = parseRefs(code, "applesauce4");
        Assertions.assertEquals(1, refResponse.references().size(), refResponse.toString());
    }

}
//postfixExpression:
//        primaryExpression
//        | postfixExpression LeftBracket (expression | bracedInitList) RightBracket
//        | postfixExpression LeftParen expressionList? RightParen
//        | (simpleTypeSpecifier | typeNameSpecifier) (
//        LeftParen expressionList? RightParen
//        | bracedInitList
//        )
//        | postfixExpression (Dot | Arrow) (
//        Template? idExpression
//        | pseudoDestructorName
//        )
//        | postfixExpression (PlusPlus | MinusMinus)
//        | (
//        Dynamic_cast
//        | Static_cast
//        | Reinterpret_cast
//        | Const_cast
//        ) Less theTypeId Greater LeftParen expression RightParen
//        | typeIdOfTheTypeId LeftParen (expression | theTypeId) RightParen;



//(translationUnit
//
//
//        (declarationseq
//
//        (declaration
//        (functionDefinition (declSpecifierSeq (declSpecifier (typeSpecifier (trailingTypeSpecifier (simpleTypeSpecifier int))))) (declarator (pointerDeclarator (noPointerDeclarator (noPointerDeclarator (declaratorid (idExpression (unqualifiedId foo)))) (parametersAndQualifiers ( )))))
//
//        (functionBody (compoundStatement { }))))
//
//        (declaration
//        (functionDefinition (declSpecifierSeq (declSpecifier (typeSpecifier (trailingTypeSpecifier (simpleTypeSpecifier int))))) (declarator (pointerDeclarator (noPointerDeclarator (noPointerDeclarator (declaratorid (idExpression (unqualifiedId bar)))) (parametersAndQualifiers ( )))))
//
//        (functionBody (compoundStatement { (statementSeq (statement (declarationStatement (blockDeclaration (simpleDeclaration (initDeclaratorList (initDeclarator (declarator (pointerDeclarator (noPointerDeclarator (noPointerDeclarator (declaratorid (idExpression (unqualifiedId foo)))) (parametersAndQualifiers ( ))))))) ";"))))) }))))) <EOF>)
