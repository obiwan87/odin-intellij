package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.CachedValue;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.lang.psi.OdinReference;
import com.lasagnerd.odin.lang.psi.OdinReferenceOwner;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public abstract class OdinReferenceOwnerMixin extends OdinPsiElementImpl implements OdinReferenceOwner {
    private CachedValue<OdinReference> cachedReference;

    public OdinReferenceOwnerMixin(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public abstract OdinReference getReference();

    public OdinSymbol getReferencedSymbol() {
        return getReference().getSymbol();
    }
}