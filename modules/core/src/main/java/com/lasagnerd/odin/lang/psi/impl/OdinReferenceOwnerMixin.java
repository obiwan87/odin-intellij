package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.CachedValue;
import com.lasagnerd.odin.lang.psi.OdinReference;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class OdinReferenceOwnerMixin extends OdinPsiElementImpl {
    private CachedValue<OdinReference> cachedReference;

    public OdinReferenceOwnerMixin(@NotNull ASTNode node) {
        super(node);
    }
}
