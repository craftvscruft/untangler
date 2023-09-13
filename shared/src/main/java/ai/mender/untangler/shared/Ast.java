package ai.mender.untangler.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Ast(String kind, SourceRange range, List<Ast> children, Map<String, String> props) {

    public static Ast create(String kind, SourceRange range) {
        return new Ast(kind, range, new ArrayList<>(), new HashMap<>());
    }
}
