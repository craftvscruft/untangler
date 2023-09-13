package ai.mender.strategy;

import ai.mender.SimpleSelector;
import ai.mender.domain.ReferencesResponse;
import ai.mender.untangler.shared.SourceRange;
import ai.mender.parsing.Ast;
import ai.mender.strategy.cpp.Scope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class TestScope {

    private Scope subject;
    private Ast root;
    private SourceRange range1;
    private SourceRange range2;
    private SourceRange range3;

    @BeforeEach
    public void setup() {
        subject = new Scope(SimpleSelector.parse("*"));
        range1 = SourceRange.parse("1:1-1:1");
        range2 = SourceRange.parse("2:1-2:1");
        range3 = SourceRange.parse("3:1-3:1");
        root = new Ast("root", null, new ArrayList<>(), range1);
    }
    @Test
    public void overridingDeclarations() {
        subject.addDeclaration(new Ast("foo", root, new ArrayList<>(), range1));
        subject.addRef(new Ast("foo", root, new ArrayList<>(), range2));
        subject.addOverridingDeclaration(new Ast("foo", root, new ArrayList<>(), range3));
        ReferencesResponse response = subject.getReferences();

        Assertions.assertEquals(1, response.declarations().size(), response.toString());
        Assertions.assertEquals(2, response.references().size());
        Assertions.assertEquals(range2, response.references().get(0).range()); // order not important here
        Assertions.assertEquals(range1, response.references().get(1).range());
        Assertions.assertEquals(range3, response.declarations().get(0));
    }


}
