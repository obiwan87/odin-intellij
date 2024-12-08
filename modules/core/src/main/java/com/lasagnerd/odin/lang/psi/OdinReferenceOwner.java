package com.lasagnerd.odin.lang.psi;

import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import org.jetbrains.annotations.NotNull;

public interface OdinReferenceOwner extends OdinPsiElement {

    @NotNull
    OdinReference getReference();

    // TODO add getReferencedSymbol() with OdinContext
    default OdinSymbol getReferencedSymbol() {
        return getReference().getSymbol();
    }

    @NotNull
    OdinReference getReference(OdinContext context);

    default OdinSymbol getReferencedSymbol(OdinContext context) {
        return getReference(context).getSymbol();
    }
}
