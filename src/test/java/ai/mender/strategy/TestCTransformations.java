package ai.mender.strategy;

import ai.mender.domain.SourcePosition;
import ai.mender.parsing.Ast;
import ai.mender.parsing.SyntaxTreeUtil;
import ai.mender.strategy.cpp.CppStrategy;
import ai.mender.strategy.cpp.CppTopLevelNode;
import org.apache.commons.lang3.StringUtils;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TestCTransformations {
    @Test
    public void transformedAstGroupsVariableDeclarationsAndReferences() {
        String code = """
            double global1;
            void foo(double arg1, double arg2) {
              double s = currentPoint = calculateBezierPoint(t) - calculateBezierPoint(u);
              global1 = arg1;
            }""";

        // Final pass should look something like this:
        // (root
        //  (decl global1)
        //  (fn
        //    (name foo)
        //    (params arg1 arg1)
        //    (fnBody
        //      (decl s)
        //      (ref currentPoint calculateBezierPoint t calculateBezierPoint u global1 arg1))))
        //

//        (root
//                (fn
//                        (name arg1)
//        (params arg1)
//        (fnBody
//                (decl initializer)
//        (ref calculateBezierPoint t calculateBezierPoint u simpleDecl initializer))))
        SourceFile.StringSourceFile sourceFile = new SourceFile.StringSourceFile("main.c", code);
        CppTopLevelNode topLevelNode = new CppStrategy().parseTopLevel(sourceFile);
        List<String> astPhases = topLevelNode.exposeAstPasses();
        Approvals.verify(StringUtils.join(astPhases, "\n====PASS===\n"));
    }

    @Test
    public void astTransformationKeepsSourceRangesOfFunctionName() {
        String code = """
            double global1;
            void foo(double arg1, double arg2) {
              double s = currentPoint = calculateBezierPoint(t) - calculateBezierPoint(u);
              global1 = arg1;
            }""";
        SourceFile.StringSourceFile sourceFile = new SourceFile.StringSourceFile("main.c", code);
        CppTopLevelNode topLevelNode = new CppStrategy().parseTopLevel(sourceFile);
        Ast ast = CppStrategy.getTransformedAst(CppStrategy.parseAst(topLevelNode));
        Optional<Ast> fnNode = ast.getFirstChildByTag("fn");
        Assertions.assertFalse(fnNode.isEmpty());
        Optional<Ast> nameNode = fnNode.get().getFirstChildByTag("name");
        Assertions.assertFalse(nameNode.isEmpty());
        Assertions.assertEquals(new SourcePosition(2, 6), nameNode.get().range().start());
        Assertions.assertEquals(new SourcePosition(2, 9), nameNode.get().range().end());
    }

    @Test
    public void astTransformationKeepsSourceRangesOfParamName() {
        String code = """
            void foo(double arg1, double arg2) {
            }""";
        SourceFile.StringSourceFile sourceFile = new SourceFile.StringSourceFile("main.c", code);
        CppTopLevelNode topLevelNode = new CppStrategy().parseTopLevel(sourceFile);
        Ast ast = CppStrategy.getTransformedAst(CppStrategy.parseAst(topLevelNode));
        Optional<Ast> fnNode = ast.getFirstChildByTag("fn");
        Assertions.assertFalse(fnNode.isEmpty());
        Optional<Ast> params = fnNode.get().getFirstChildByTag("params");
        Assertions.assertFalse(params.isEmpty());
        List<Ast> paramNodes = params.get().children();
        Assertions.assertEquals(2, paramNodes.size(), paramNodes.toString());
        Assertions.assertEquals(new SourcePosition(1, 17), paramNodes.get(0).range().start());
        Assertions.assertEquals(new SourcePosition(1, 21), paramNodes.get(0).range().end());
        Assertions.assertEquals(new SourcePosition(1, 30), paramNodes.get(1).range().start());
        Assertions.assertEquals(new SourcePosition(1, 34), paramNodes.get(1).range().end());
    }

    @Test
    public void astTransformationKeepsSourceRangesOfLocalVars() {
        String code = """
            void foo(double arg1, double arg2) {
              double s = currentPoint = calculateBezierPoint(t) - calculateBezierPoint(u);
              global1 = arg1;
            }""";
        SourceFile.StringSourceFile sourceFile = new SourceFile.StringSourceFile("main.c", code);
        CppTopLevelNode topLevelNode = new CppStrategy().parseTopLevel(sourceFile);
        Ast ast = CppStrategy.getTransformedAst(CppStrategy.parseAst(topLevelNode));

        Optional<Ast> fnNode = ast.getFirstChildByTag("fn");
        Assertions.assertFalse(fnNode.isEmpty());
        Optional<Ast> fnBody = fnNode.get().getFirstChildByTag("fnBody");
        Assertions.assertFalse(fnBody.isEmpty());
        Optional<Ast> declNode = fnBody.get().getFirstChildByTag("decl");
        Optional<Ast> refNode = fnBody.get().getFirstChildByTag("ref");
        Assertions.assertFalse(declNode.isEmpty());
        Assertions.assertFalse(declNode.get().children().isEmpty());
        Ast declaredVarNode = declNode.get().children().get(0);
        Assertions.assertFalse(refNode.isEmpty());

        Assertions.assertEquals("s", declaredVarNode.tag());
        Assertions.assertEquals(new SourcePosition(2, 10), declaredVarNode.range().start());
        Assertions.assertEquals(new SourcePosition(2, 11), declaredVarNode.range().end());
        String[] refNames = "currentPoint,calculateBezierPoint,t,calculateBezierPoint,u,global1,arg1".split(",");
        Assertions.assertEquals(Arrays.asList(refNames), refNode.get().childTags());
        Ast firstRef = refNode.get().children().get(0);
        Ast secondRef = refNode.get().children().get(1);
        System.out.println(SyntaxTreeUtil.astToSexprWithRanges(ast));
        Assertions.assertEquals(new SourcePosition(2,14), firstRef.range().start());
        Assertions.assertEquals(new SourcePosition(2,14 + "currentPoint".length()), firstRef.range().end());

        Assertions.assertEquals(new SourcePosition(2,29), secondRef.range().start());
        Assertions.assertEquals(new SourcePosition(2,29 + "calculateBezierPoint".length()), secondRef.range().end());
    }
}
