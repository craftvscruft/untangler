package ai.mender.untangler.javaparser;


import ai.mender.untangler.shared.ISourceFile;
import ai.mender.untangler.shared.LanguageEngine;
import ai.mender.untangler.shared.response.CommentRec;
import ai.mender.untangler.shared.response.FunctionRec;
import ai.mender.untangler.shared.response.SourcePosition;
import ai.mender.untangler.shared.response.SourceRange;
import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;


import java.io.Reader;
import java.util.List;
import java.util.stream.Stream;


public class JavaParserEngine implements LanguageEngine {


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

    private static SourceRange toSourceRange(Node node) {
        Position begin = node.getRange().get().begin;
        Position end = node.getRange().get().end;
        SourcePosition startPos = new SourcePosition(begin.line, begin.column);
        SourcePosition endPos = new SourcePosition(end.line, end.column + 1);
        return new SourceRange(startPos, endPos);
    }

}
