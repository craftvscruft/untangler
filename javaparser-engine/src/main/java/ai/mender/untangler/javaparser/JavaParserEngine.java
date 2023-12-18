package ai.mender.untangler.javaparser;


import ai.mender.untangler.shared.ISourceFile;
import ai.mender.untangler.shared.LanguageEngine;
import ai.mender.untangler.shared.response.FunctionRec;
import ai.mender.untangler.shared.response.SourcePosition;
import ai.mender.untangler.shared.response.SourceRange;
import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import com.github.javaparser.ast.body.MethodDeclaration;


import java.io.Reader;
import java.util.List;
import java.util.stream.Stream;


public class JavaParserEngine implements LanguageEngine {


    @Override
    public Stream<FunctionRec> functions(ISourceFile sourceFile) {
        CompilationUnit cu = StaticJavaParser.parse(sourceFile.getReader());
        return cu.findAll(MethodDeclaration.class).stream().map((MethodDeclaration node) ->
                new FunctionRec(node.getName().getIdentifier(), toSourceRange(node))
        );
    }

    private static SourceRange toSourceRange(MethodDeclaration node) {
        Position begin = node.getRange().get().begin;
        Position end = node.getRange().get().end;
        SourcePosition startPos = new SourcePosition(begin.line, begin.column);
        SourcePosition endPos = new SourcePosition(end.line, end.column + 1);
        return new SourceRange(startPos, endPos);
    }

}
