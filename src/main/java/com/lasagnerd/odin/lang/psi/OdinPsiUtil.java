package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiReference;

public class OdinPsiUtil {
    public static PsiReference getReference(OdinPrimary self) {
        return new OdinReferenceExpression(self);
    }

}
