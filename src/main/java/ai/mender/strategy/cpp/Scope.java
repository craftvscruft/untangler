package ai.mender.strategy.cpp;

import ai.mender.SimpleSelector;
import ai.mender.domain.Reference;
import ai.mender.domain.ReferencesResponse;
import ai.mender.domain.SourceRange;
import ai.mender.parsing.Ast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Scope {
    final private Logger LOG = LoggerFactory.getLogger(Scope.class);
    private final ReferencesResponse referenceResponse;
    Stack<Map<String, Ast>> varNameToDeclScope = new Stack<>();
    Map<Ast, Ast> refToDecl = new HashMap<>();

    List<Ast> overridingDecls = new ArrayList<>();

    public Scope(SimpleSelector selector) {
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
                .filter(ref -> selector.matchesLineRange(ref.range()) || selector.matchesLineRange(ref.declarationRange()))
                .toList();
        Stream<SourceRange> matchedDeclarations = referenceResponse.declarations()
                .stream()
                .filter(range -> selector.matchesLineRange(range));
        Stream<SourceRange> declarationsFromMatchedReferences = matchedReferences.stream()
                .map(Reference::declarationRange);
        List<SourceRange> combinedDeclarations = Stream.concat(matchedDeclarations, declarationsFromMatchedReferences)
                .distinct().toList();
        ReferencesResponse filteredResponse = new ReferencesResponse(selector, combinedDeclarations, matchedReferences);
        LOG.info("After line filter\nDeclarations {}\nReferences {}", filteredResponse.declarations(), filteredResponse.references());
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

    public void addOverridingDeclaration(Ast ast) {
        // This is for separate function declarations/definitions.
        // Convert previous declarations to references
        String name = ast.tag();
        boolean matchesName = getSelector().matchesName(name);
        LOG.info("Saw ref {}, matchesName {}", name, matchesName);
        if (matchesName) {
            Map<String, Ast> currentScope = this.varNameToDeclScope.peek();
            this.referenceResponse.declarations().add(ast.range());
            Ast oldDeclNode = currentScope.get(name);
            if (null != oldDeclNode) {
                this.referenceResponse.declarations().remove(oldDeclNode.range());
                System.out.println(this.referenceResponse.declarations().size());
                Predicate<Reference> isRefToOldDecl = ref -> ref.declarationRange().equals(oldDeclNode.range());
                List<Reference> updatedReferences = this.referenceResponse.references().stream().filter(isRefToOldDecl).map(oldRef ->
                        new Reference(oldRef.name(), oldRef.range(), ast.range())
                ).toList();
                this.referenceResponse.references().removeIf(isRefToOldDecl);
                this.referenceResponse.references().addAll(updatedReferences);

                this.referenceResponse.references().add(new Reference(name, oldDeclNode.range(), ast.range()));
            }
            currentScope.put(name, ast);
        }
    }
}
