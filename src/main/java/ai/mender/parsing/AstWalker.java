package ai.mender.parsing;

import ai.mender.strategy.TreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AstWalker {
    final Logger LOG = LoggerFactory.getLogger(AstWalker.class);
    private final AstListenerRegistry listenerRegistry = new AstListenerRegistry();
    private Ast current;

    public AstWalker() {
    }
    public boolean hasAncestorWithTag(String tag) {
        if (current == null) {
            return false;
        }
        Ast node = current.parent();
        while (node != null) {
            if (tag.equals(node.tag())) {
                return true;
            }
            node = node.parent();
        }
        return false;
    }
    public List<String> parentTagsStoppingAt(String stopAtTag) {
        List<String> parentTags = new ArrayList<>();
        if (current == null) {
            return parentTags;
        }

        Ast node = current.parent();
        while (node != null) {
            parentTags.add(node.tag());
            if (stopAtTag.equals(node.tag())) {
                return parentTags;
            }
            node = node.parent();
        }
        return parentTags;
    }

    public void walk(Ast root) {
        LOG.debug("Starting walk at AST:\n{}", SyntaxTreeUtil.astToSexpr(root));
        LOG.debug("Registered enter handlers: {}", getListenerRegistry().enterRegistry.keySet());
        LOG.debug("Registered exit handlers: {}", getListenerRegistry().exitRegistry.keySet());
        walkInner(root);
    }

    private void walkInner(Ast current) {
        this.current = current;
        LOG.debug("Walking node {} {}", current.tag(), current.range());
        for (Ast child : current.children()) {
            String childTag = child.tag();
            LOG.debug("Calling entry listener for {}", childTag);
            this.current = child;
            getListenerRegistry().enter(child);
            walkInner(child);
            LOG.debug("Calling exit listener for {}", childTag);
            getListenerRegistry().exit(child);
        }
    }

    public void registerTrackingListener(String tag, TreeBuilder treeBuilder) {
        getListenerRegistry().listenForTag(tag, node -> {
            treeBuilder.enter(tag);
        }, node -> {
            treeBuilder.exit();
        });
    }

    public AstListenerRegistry getListenerRegistry() {
        return listenerRegistry;
    }
}
