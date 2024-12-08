package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.util.CachedValue;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import org.jetbrains.annotations.NotNull;

public interface OdinReferenceOwner extends OdinPsiElement {

    CachedValue<OdinReference> getCachedReference();

    void setCachedReference(CachedValue<OdinReference> cachedReference);

    @NotNull
    OdinReference getReference();

    // TODO add getReferencedSymbol() with OdinContext
    default OdinSymbol getReferencedSymbol() {
        return getReference().getSymbol();
    }
}
