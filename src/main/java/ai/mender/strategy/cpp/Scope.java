package ai.mender.strategy.cpp;

import ai.mender.domain.Reference;
import ai.mender.domain.ReferencesResponse;
import ai.mender.domain.SourceRange;
import ai.mender.parsing.Ast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Scope {
    final private Logger LOG = LoggerFactory.getLogger(Scope.class);
    private final ReferencesResponse referenceResponse;
    Stack<Map<String, Ast>> varNameToDeclScope = new Stack<>();
    Map<Ast, Ast> refToDecl = new HashMap<>();

    Scope(String nameToFilter) {
        this.referenceResponse = ReferencesResponse.empty(nameToFilter);
        this.varNameToDeclScope.push(new HashMap<>());
    }

    public void addDeclaration(Ast child) {
        String name = child.tag();
        boolean matches = nameMatchesFilter(name);
        LOG.info("Saw ref {}, matches {}", name, matches);
        if (matches) {
            this.varNameToDeclScope.peek().put(name, child);
            this.referenceResponse.declarations().add(child.range());
        }
    }

    boolean nameMatchesFilter(String name) {
        return referenceResponse.name().equals(name) || referenceResponse.name().equals("*");
    }

    public void addRef(Ast refNode) {
        String name = refNode.tag();
        boolean matches = nameMatchesFilter(name);
        LOG.info("Saw ref {}, matches {}", name, matches);
        if (matches) {

            Ast declNode = this.varNameToDeclScope.peek().get(name);
            LOG.info("Resolved decl {} for ref {}", declNode, refNode);
            // declNode can be null, which indicates unknown
            this.refToDecl.put(refNode, declNode);
            SourceRange declRange = declNode == null ? SourceRange.unknown() : declNode.range();
            this.referenceResponse.references().add(new Reference(name, refNode.range(), declRange));
        }

    }

    public void pop() {
        LOG.info("POP scope stack");
        this.varNameToDeclScope.pop();
    }

    public void push() {
        LOG.info("PUSH scope stack");
        this.varNameToDeclScope.push(new HashMap<>(this.varNameToDeclScope.peek()));
    }

    public ReferencesResponse getReferences() {
        return this.referenceResponse;
    }

    public void resolveUnknownFromCurrentScope() {
        // Call this at the end to resolve things references before they are declared.
        List<Reference> references = referenceResponse.references();
        for (int i = 0; i < references.size(); i++) {
            Reference reference = references.get(i);
            Map<String, Ast> currentScope = this.varNameToDeclScope.peek();
            String name = reference.name();
            boolean isUnknownButDeclaredInCurrentScope = reference.declarationRange().isUnknown() &&
                    currentScope.containsKey(name);
            if (isUnknownButDeclaredInCurrentScope) {
                SourceRange resolvedRange = currentScope.get(name).range();
                references.set(i, new Reference(name, reference.range(), resolvedRange));
            }
        }
    }
}
