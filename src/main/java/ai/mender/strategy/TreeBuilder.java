package ai.mender.strategy;

import ai.mender.domain.SourceRange;
import ai.mender.parsing.Ast;
import ai.mender.parsing.SyntaxTreeUtil;

import java.util.*;


public class TreeBuilder {
    public static final String INDENT = "  ";
    Ast root = new Ast("root", null, new ArrayList<>(), null);
    Ast current = root;
    Stack<Map<String, List<String>>> stack = new Stack<>();
    private SourceRange currentRange = null;

    {
        stack.push(new LinkedHashMap<>());
    }

    public TreeBuilder enter(String tag) {
        Ast newNode = new Ast(tag, current, new ArrayList<>(), currentRange);
        current.addChild(newNode);
        current = newNode;
        return this;
    }

    public TreeBuilder exit() {
        if (current.parent() == null) {
            throw new RuntimeException("We came too far");
        }
        current = current.parent();
        return this;
    }


    public void accumulate(String tag, String atom) {
        Optional<Ast> first = current.getFirstChildByTag(tag);
        first.ifPresentOrElse(
                (found) -> found.addChild(new Ast(atom, found, new ArrayList<>(), currentRange)),
                () -> enter(tag).addAtom(atom).exit()
        );
    }

    public void addFirstOrAccumulate(String first, String rest, String atom) {
        Optional<Ast> firstChildByTag = current.getFirstChildByTag(first);
        if (firstChildByTag.isEmpty()) {
            enter(first).addAtom(atom).exit();
        } else {
            accumulate(rest, atom);
        }
    }


    public String toSexpr() {
        return SyntaxTreeUtil.astToSexpr(this.root);
    }

    public TreeBuilder addAtom(String tag) {
        return enter(tag).exit();
    }

    public Ast root() {
        return root;
    }

    public void setCurrentRange(SourceRange currentRange) {
        this.currentRange = currentRange;
    }


    public record AstDepth(Ast node, int depth) {
    }


}

