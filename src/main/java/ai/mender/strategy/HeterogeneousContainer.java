package ai.mender.strategy;


import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class HeterogeneousContainer {
    // https://www.informit.com/articles/article.aspx?p=2861454&seqNum=8
    private Map<Class<? extends ParserRuleContext>, Consumer<ParserRuleContext>> enterListeners = new HashMap<>();
    private Map<Class<? extends ParserRuleContext>, Consumer<ParserRuleContext>> exitListeners = new HashMap<>();
    @SuppressWarnings("unchecked")
    public <T extends ParserRuleContext> void register(Class<T> type, Consumer<T> enter, Consumer<T> exit) {
        enterListeners.put(Objects.requireNonNull(type), node -> enter.accept((T)(node)));
        exitListeners.put(Objects.requireNonNull(type), node -> exit.accept((T)(node)));
    }

//    @SuppressWarnings("unchecked")
//    public <T extends ParserRuleContext> Consumer<T> getFavorite(Class<T> type) {
//        return (Consumer<T>) favorites.get(type);
//    }

    public void acceptEnter(ParserRuleContext ctx) {
        var consumer = enterListeners.get(ctx.getClass());
        if (consumer != null) {
            consumer.accept(ctx);
        }
    }

    public void acceptExit(ParserRuleContext ctx) {
        var consumer = exitListeners.get(ctx.getClass());
        if (consumer != null) {
            consumer.accept(ctx);
        }
    }
}