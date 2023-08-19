package ai.mender.strategy.cpp;

import ai.mender.SimpleSelector;
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
import java.util.stream.Stream;

class Scope {
    final private Logger LOG = LoggerFactory.getLogger(Scope.class);
    private final ReferencesResponse referenceResponse;
    Stack<Map<String, Ast>> varNameToDeclScope = new Stack<>();
    Map<Ast, Ast> refToDecl = new HashMap<>();

    Scope(SimpleSelector selector) {
        this.referenceResponse = ReferencesResponse.empty(selector);
        this.varNameToDeclScope.push(new HashMap<>());
    }

    public void addDeclaration(Ast child) {
        String name = child.tag();
        boolean matchesName = getSelector().matchesName(name);
        LOG.info("Saw ref {}, matchesName {}", name, matchesName);
        if (matchesName) {
            this.varNameToDeclScope.peek().put(name, child);
            this.referenceResponse.declarations().add(child.range());
        }
    }

    private SimpleSelector getSelector() {
        return this.referenceResponse.selector();
    }


    public void addRef(Ast refNode) {
        String name = refNode.tag();
        boolean matchesName = getSelector().matchesName(name);
        LOG.info("Saw ref {}, matchesName {}", name, matchesName);
        if (matchesName) {
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
        LOG.info("Before line filter\nDeclarations {}\nReferences {}", referenceResponse.declarations(), referenceResponse.references());
        SimpleSelector selector = referenceResponse.selector();
        List<Reference> matchedReferences = referenceResponse.references()
                .stream()
                .filter(ref -> selector.matchesLine(ref.range()) || selector.matchesLine(ref.declarationRange()))
                .toList();
        Stream<SourceRange> matchedDeclarations = referenceResponse.declarations()
                .stream()
                .filter(range -> selector.matchesLine(range));
        Stream<SourceRange> declarationsFromMatchedReferences = matchedReferences.stream()
                .map(Reference::declarationRange);
        List<SourceRange> combinedDeclarations = Stream.concat(matchedDeclarations, declarationsFromMatchedReferences)
                .distinct().toList();
        ReferencesResponse filteredResponse = new ReferencesResponse(selector, combinedDeclarations, matchedReferences);
        LOG.info("After line filter\nDeclarations {}\nReferences {}", referenceResponse.declarations(), referenceResponse.references());
        return filteredResponse;
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
