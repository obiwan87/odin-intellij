package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.lang.psi.OdinImportDeclarationStatement;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Scope {
    @Getter
    private String packagePath;
    public static final Scope EMPTY = new Scope();
    Map<String, PsiNamedElement> symbolTable = new HashMap<>();

    public Scope() {

    }


    @Nullable
    public PsiNamedElement findNamedElement(String name) {
        return symbolTable.get(name);
    }

    public Collection<PsiNamedElement> getNamedElements() {
        return symbolTable.values();
    }

    public Collection<PsiNamedElement> getFiltered(Predicate<? super PsiNamedElement> predicate) {
        return symbolTable.values().stream().filter(predicate).toList();
    }

    public void addAll(Collection<? extends PsiNamedElement> namedElements) {
        for (PsiNamedElement namedElement : namedElements) {
            symbolTable.put(namedElement.getName(), namedElement);
        }
    }

    public void add(PsiNamedElement namedElement) {
        symbolTable.put(namedElement.getName(), namedElement);
    }

    static Scope from(Collection<? extends PsiNamedElement> identifiers) {
        if (identifiers.isEmpty())
            return Scope.EMPTY;

        Scope scope = new Scope();
        for (var declaredIdentifier : identifiers) {
            scope.symbolTable.put(declaredIdentifier.getName(), declaredIdentifier);
        }

        return scope;
    }

    static Scope from(List< ? extends PsiNamedElement> identifiers, String packagePath) {
        Scope scope = from(identifiers);
        scope.packagePath = packagePath;

        return scope;
    }

    public Scope with(List<? extends PsiNamedElement> identifiers) {
        Scope scope = from(identifiers);
        scope.packagePath = this.packagePath;

        return scope;
    }

    public Scope with(String packagePath) {
        Scope scope = new Scope();
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

    public Scope getScopeOfImport(String packageIdentifier) {
        PsiNamedElement psiNamedElement = symbolTable.get(packageIdentifier);
        if(psiNamedElement instanceof  OdinImportDeclarationStatement importDeclarationStatement) {
            return OdinInsightUtils.getDeclarationsOfImportedPackage(this, importDeclarationStatement);
        }
        throw new RuntimeException("namedElement " + packageIdentifier + " is not of type importDeclarationStatement.");
    }

}
