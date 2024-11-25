package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;

public interface OdinReferenceOwner extends PsiElement {

    CachedValue<OdinReference> getCachedReference();

    void setCachedReference(CachedValue<OdinReference> cachedReference);

    OdinReference getReference();
}
