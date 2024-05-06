package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.insights.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinImportDeclarationStatement;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Getter
public class OdinScope {
    @Setter
    private String packagePath;
    public static final OdinScope EMPTY = new OdinScope();
    Map<String, PsiNamedElement> symbolTable = new HashMap<>();

    /**
     * keeps track of the types
     */
    Map<String, TsOdinType> typeTable = new HashMap<>();

    public OdinScope() {

    }


    @Nullable
    public PsiNamedElement getNamedElement(String name) {
        return symbolTable.get(name);
    }

    public TsOdinType getType(String polymorphicParameter) {
        return typeTable.get(polymorphicParameter);
    }

    public void addType(String polymorphicParameter, TsOdinType type) {
        typeTable.put(polymorphicParameter, type);
    }

    public Collection<PsiNamedElement> getNamedElements() {
        return symbolTable.values();
    }

    public Collection<PsiNamedElement> getFiltered(Predicate<? super PsiNamedElement> predicate) {
        return symbolTable.values().stream().filter(predicate).toList();
    }

    public void addAll(Collection<? extends PsiNamedElement> namedElements) {
        addAll(namedElements, true);
    }

    public void addAll(Collection<? extends PsiNamedElement> namedElements, boolean override) {
        for (PsiNamedElement namedElement : namedElements) {
            if(!symbolTable.containsKey(namedElement.getName()) || !override) {
                symbolTable.put(namedElement.getName(), namedElement);
            }
        }
    }

    public void putAll(OdinScope scope) {
        symbolTable.putAll(scope.symbolTable);
        typeTable.putAll(scope.typeTable);
    }

    public void addTypes(OdinScope scope) {
        typeTable.putAll(scope.typeTable);
    }

    public void add(PsiNamedElement namedElement) {
        add(namedElement, true);
    }

    public void add(PsiNamedElement namedElement, boolean override) {
        if(!override)
            symbolTable.put(namedElement.getName(), namedElement);
        else if(!symbolTable.containsKey(namedElement.getName())) {
            symbolTable.put(namedElement.getName(), namedElement);
        }

    }

    static OdinScope from(Collection<? extends PsiNamedElement> identifiers) {
        if (identifiers.isEmpty())
            return OdinScope.EMPTY;

        OdinScope scope = new OdinScope();
        for (var declaredIdentifier : identifiers) {
            scope.symbolTable.put(declaredIdentifier.getName(), declaredIdentifier);
        }

        return scope;
    }

    static OdinScope from(List<? extends PsiNamedElement> identifiers, String packagePath) {
        OdinScope scope = from(identifiers);
        scope.packagePath = packagePath;

        return scope;
    }

    public OdinScope with(List<? extends PsiNamedElement> identifiers) {
        OdinScope scope = from(identifiers);
        scope.packagePath = this.packagePath;

        return scope;
    }

    public OdinScope with(String packagePath) {
        OdinScope scope = new OdinScope();
        scope.symbolTable = this.symbolTable;
        scope.packagePath = packagePath;

        return scope;
    }

    /**
     * Creates a new scope for a given package identifier defined within this scope.
     *
     * @param packageIdentifier The identifier that is used to reference the package
     * @return A new scope with all the declared symbols of the referenced package
     */

    public OdinScope getScopeOfImport(String packageIdentifier) {
        PsiNamedElement psiNamedElement = symbolTable.get(packageIdentifier);
        if (psiNamedElement instanceof OdinImportDeclarationStatement importDeclarationStatement) {
            return OdinInsightUtils.getDeclarationsOfImportedPackage(this, importDeclarationStatement);
        }
        return OdinScope.EMPTY;
    }

}
