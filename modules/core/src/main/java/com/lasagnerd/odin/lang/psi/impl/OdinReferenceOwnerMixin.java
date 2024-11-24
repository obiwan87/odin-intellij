package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.CachedValue;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class OdinReferenceOwnerMixin extends OdinPsiElementImpl {
    private CachedValue<PsiReference> cachedReference;

    public OdinReferenceOwnerMixin(@NotNull ASTNode node) {
        super(node);
    }
}
