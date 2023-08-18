package ai.mender.parsing;

import ai.mender.strategy.TreeBuilder;

public class AstTransformer extends AstWalker {
    public TreeBuilder treeBuilder = new TreeBuilder();

    public AstTransformer() {
        getListenerRegistry().listenForAllEnter(
                node ->
                        treeBuilder.setCurrentRange(node.range())
        );
    }

    public void registerTrackingListener(String tag) {
        registerTrackingListener(tag, treeBuilder);
    }

    public Ast transform(Ast root) {
        walk(root);
        return treeBuilder.root();
    }
}
