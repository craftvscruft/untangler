package ai.mender.parsing;

import ai.mender.untangler.shared.ISourceFile;
import ai.mender.strategy.TreeBuilder;
import ai.mender.untangler.shared.response.SourcePosition;
import ai.mender.untangler.shared.response.SourceRange;
import ai.mender.untangler.shared.response.SourceText;
import antlrgen.cpp14.CPP14Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class SyntaxTreeUtil {
    private SyntaxTreeUtil() {

    }
    public static String getTextIncludingWhitespace(ParserRuleContext ctx) {
        if (null == ctx) {
            return "";
        }
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        return ctx.start.getInputStream().getText(new Interval(a, b));
    }

    public static String getTextIncludingWhitespace(TerminalNode ctx) {
        if (null == ctx) {
            return "";
        }
        return ctx.getText();
    }


    public static SourceRange nodeToSourceRange(ParserRuleContext antlrNode) {
        int startColumn = antlrNode.start.getCharPositionInLine() + 1;
        SourcePosition start = new SourcePosition(antlrNode.start.getLine(), startColumn);
        int stopColumn = antlrNode.stop.getCharPositionInLine() + antlrNode.stop.getText().length() + 1;
        SourcePosition end = new SourcePosition(antlrNode.stop.getLine(), stopColumn);
        return new SourceRange(start, end);
    }

    public static SourceRange nodeToSourceRange(Token token) {
        int startColumn = token.getCharPositionInLine() + 1;
        int startLine = token.getLine();
        SourcePosition start = new SourcePosition(startLine, startColumn);
        List<String> lines = token.getText().lines().toList();
        int lastLineLength = lines.get(lines.size() - 1).length();
        int endLine = startLine + lines.size() - 1;
        int stopColumn = lines.size() > 1 ? (lastLineLength + 1) : startColumn + lastLineLength;
        SourcePosition end = new SourcePosition(endLine, stopColumn);
        return new SourceRange(start, end);
    }

    public static String astToSexpr(Ast tree) {
        return astToSexpr(tree, false);
    }

    public static String astToSexprWithRanges(Ast ast) {
        return astToSexpr(ast, true);
    }

    private static String astToSexpr(Ast tree, boolean includeRanges) {
        final String INDENT = "  ";
        StringBuilder stringBuilder = new StringBuilder();
        Stack<TreeBuilder.AstDepth> openNodes = new Stack<>();
        openNodes.add(new TreeBuilder.AstDepth(tree, 0));

        int prevDepth = 0;
        while (!openNodes.isEmpty()) {
            TreeBuilder.AstDepth nodeDepth = openNodes.pop();
            Ast node = nodeDepth.node();
            int depth = nodeDepth.depth();
            if (depth < prevDepth) {
                stringBuilder.append(")".repeat(prevDepth - depth));
            }
            if (depth > 0) {
                stringBuilder.append("\n").append(INDENT.repeat(depth));
            }
            if (node.isChildless()) {
                stringBuilder.append(renderNodeTag(node, includeRanges));
            } else {
                stringBuilder.append("(");
                stringBuilder.append(renderNodeTag(node, includeRanges));

                if (node.allChildrenAreChildless()) {
                    stringBuilder.append(" ").append(node.children().stream().map(Ast::tag).collect(Collectors.joining(" ")));
                    stringBuilder.append(")");
                } else {
                    node.reverseChildrenStream()
                            .map(child -> new TreeBuilder.AstDepth(child, depth+1))
                            .forEachOrdered(openNodes::push);
                }
            }
            prevDepth = depth;
        }
        stringBuilder.append(")".repeat(prevDepth));
        return stringBuilder.toString();
    }

    private static String renderNodeTag(Ast node, boolean includeRange) {
        if (includeRange) {
            return node.tag() + (node.range() == null ? "[missing]" : node.range().toString());
        }
        return node.tag();
    }


    public static SourceText nodeToSourceText(CPP14Parser.InitDeclaratorContext ctx) {
        return new SourceText(
                getTextIncludingWhitespace(ctx),
                nodeToSourceRange(ctx));
    }

    public static CharStream fileToCharStream(ISourceFile file) {
        try {
            return CharStreams.fromReader(file.getReader(), file.getName());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
