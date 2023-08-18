package ai.mender.parsing;


import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.function.Consumer;

public class AstListenerRegistry {
    public static final String ALL = "*";
    public static final String UNMATCHED = "<UNMATCHED>";
    Multimap<String, Consumer<Ast>> enterRegistry = MultimapBuilder.hashKeys().linkedListValues().build();
    Multimap<String, Consumer<Ast>> exitRegistry = MultimapBuilder.hashKeys().linkedListValues().build();

    public AstListenerRegistry() {
    }

    public void enter(Ast child) {
        applyRegistryToNode(enterRegistry, child);
        enterRegistry.get(ALL).forEach(listener -> listener.accept(child));
    }

    private void applyRegistryToNode(Multimap<String, Consumer<Ast>> registry, Ast child) {
        String tag = child.tag();
        if (registry.containsKey(tag)) {
            registry.get(tag).forEach(listener -> listener.accept(child));
        } else {
            registry.get(UNMATCHED).forEach(listener -> listener.accept(child));
        }
    }

    public void exit(Ast child) {
        applyRegistryToNode(exitRegistry, child);
    }

    public void listenForUnmatched(Consumer<Ast> enterListener, Consumer<Ast> exitListener) {
        enterRegistry.put(UNMATCHED, enterListener);
        exitRegistry.put(UNMATCHED, exitListener);
    }

    public void listenForTag(String tag, Consumer<Ast> enterListener, Consumer<Ast> exitListener) {
        enterRegistry.put(tag, enterListener);
        exitRegistry.put(tag, exitListener);
    }

    public void listenForAllEnter(Consumer<Ast> enterListener) {
        enterRegistry.put(ALL, enterListener);
    }
}