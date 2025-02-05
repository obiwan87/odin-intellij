package com.lasagnerd.odin.lang.psi;

import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OdinReferenceOwner extends OdinPsiElement {

    @Nullable
    OdinReference getReference();

    default OdinSymbol getReferencedSymbol() {
        OdinReference reference = getReference();
        if (reference != null) {
            return reference.getSymbol();
        }
        return null;
    }

    @Nullable
    OdinReference getReference(OdinContext context);

    default OdinSymbol getReferencedSymbol(@NotNull OdinContext context) {
        OdinReference reference = getReference(context);
        if (reference != null) {
            return reference.getSymbol();
        }
        return null;
    }
}
