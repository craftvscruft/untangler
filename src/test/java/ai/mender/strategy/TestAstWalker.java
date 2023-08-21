package ai.mender.strategy;

import ai.mender.domain.SourcePosition;
import ai.mender.domain.SourceRange;
import ai.mender.parsing.Ast;
import ai.mender.parsing.AstListenerRegistry;
import ai.mender.parsing.AstWalker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TestAstWalker {

    private AstWalker subject;

    @BeforeEach
    public void setup() {
        subject = new AstWalker();
    }
    @Test
    public void astWalker() {
        TreeBuilder treeBuilder = new TreeBuilder();
        treeBuilder.enter("list");
        treeBuilder.enter("inner").addAtom("2").addAtom("3").exit();
        treeBuilder.accumulate("inner", "4");
        treeBuilder.exit();

        List<String> found = new ArrayList<>();
        subject.getListenerRegistry().listenForTag("list", ast -> found.add("<list>"), ast -> found.add("</list>"));
        subject.getListenerRegistry().listenForTag("inner", ast -> found.add("<inner>"), ast -> found.add("</inner>"));
        subject.getListenerRegistry().listenForUnmatched(
                ast -> found.add(String.format("<unk_%s>", ast.tag())),
                ast -> found.add(String.format("</unk_%s>", ast.tag())));
        subject.walk(treeBuilder.root);
        String expected = "<list><inner><unk_2></unk_2><unk_3></unk_3><unk_4></unk_4></inner></list>";
        Assertions.assertEquals(expected, String.join("", found));
    }

    @Test
    public void listenerRegistryWithMultipleListenersSameTag() {
        AstListenerRegistry listenerRegistry = new AstListenerRegistry();

        List<String> found = new ArrayList<>();
        listenerRegistry.listenForTag("list", ast -> found.add("<list>"), ast -> found.add("</list>"));
        listenerRegistry.listenForTag("list", ast -> found.add("<list2>"), ast -> found.add("</list2>"));
        listenerRegistry.listenForUnmatched(
                ast -> found.add(String.format("<unk_%s>", ast.tag())),
                ast -> found.add(String.format("</unk_%s>", ast.tag())));
        SourceRange range = new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 1));
        Ast listNode = new Ast("list", null, new ArrayList<>(), range);
        listenerRegistry.enter(listNode);
        Assertions.assertEquals("<list><list2>", String.join("", found));
        listenerRegistry.exit(listNode);
        Assertions.assertEquals("<list><list2></list></list2>", String.join("", found));
    }


}
