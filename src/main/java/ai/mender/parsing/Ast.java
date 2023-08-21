package ai.mender.parsing;

import ai.mender.domain.SourceRange;
import com.google.common.base.Objects;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Ast(String tag, Ast parent, List<Ast> children, SourceRange range){

    public void addChild(Ast child) {
        children().add(child);
    }
    public boolean isChildless() {
        return children.isEmpty();
    }

    public boolean allChildrenAreChildless() {
        return children.stream().allMatch(Ast::isChildless);
    }

    public Stream<Ast> reverseChildrenStream() {
        var children = children();
        IntStream reverseIndices = IntStream.range(0, children.size()).map(i -> children.size() - i - 1);
        return reverseIndices.mapToObj(children::get);
    }

    public Optional<Ast> getFirstChildByTag(String tag) {
        return children().stream().filter((child) -> tag.equals(child.tag())).findFirst();
    }

    public List<String> childTags() {
        return children.stream().map(Ast::tag).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ast ast = (Ast) o;
        return Objects.equal(tag, ast.tag) && Objects.equal(children, ast.children) && Objects.equal(range, ast.range);
    }

    @Override
    public int hashCode() {
        // Do not include parents.
        return Objects.hashCode(tag, children, range);
    }

    @Override
    public String toString() {
        return "Ast{" +
                "tag='" + tag + '\'' +
                ", children=" + childTags() +
                ", range=" + range +
                '}';
    }
}
