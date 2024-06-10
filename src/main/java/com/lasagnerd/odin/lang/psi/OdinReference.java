package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinReference extends PsiReferenceBase<OdinIdentifier> {
    public OdinReference(@NotNull OdinIdentifier element) {
        super(element);
    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return super.getAbsoluteRange();
    }

    @Override
    public @Nullable PsiElement resolve() {
        OdinSymbol firstDeclaration = OdinSymbolTableResolver.findSymbol(getElement());
        if (firstDeclaration != null) {
            return firstDeclaration.getDeclaredIdentifier();
        }
        return null;
    }

    public OdinSymbol resolveSymbol() {
        return OdinSymbolTableResolver.findSymbol(getElement());
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }
}
