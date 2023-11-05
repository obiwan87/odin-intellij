package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiReference;

public class OdinPsiUtil {
    public static PsiReference getReference(OdinIdentifierExpression self) {
        return new OdinReference(self);
    }

}
