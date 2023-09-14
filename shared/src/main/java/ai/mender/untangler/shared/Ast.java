package ai.mender.untangler.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Ast(String kind, SourceRange range, SourceRange nameRange, List<Ast> children, Map<String, String> props) {

    public static Ast create(String kind, SourceRange range, SourceRange nameRange) {
        return new Ast(kind, range, nameRange, new ArrayList<>(), new HashMap<>());
    }

    @JsonIgnore
    public Stream<Ast> dfsStream() {
        return Stream.concat(Stream.of(this), children.stream().flatMap(Ast::dfsStream));
    }
}
