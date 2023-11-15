package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiNamedElement;
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

    static Scope from(List<? extends PsiNamedElement> identifiers) {
        if (identifiers.isEmpty())
            return Scope.EMPTY;

        Scope scope = new Scope();
        for (var declaredIdentifier : identifiers) {
            scope.symbolTable.put(declaredIdentifier.getName(), declaredIdentifier);
        }

        return scope;
    }
}
