package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public interface OdinDeclaration extends OdinPsiElement {
    default List<OdinDeclaredIdentifier> getDeclaredIdentifiers() {
        List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getLocalSymbols(this);
        return localSymbols.stream().map(OdinSymbol::getDeclaredIdentifier)
                .filter(Objects::nonNull)
                .filter(OdinDeclaredIdentifier.class::isInstance)
                .map(OdinDeclaredIdentifier.class::cast)
                .toList();
    }
}
