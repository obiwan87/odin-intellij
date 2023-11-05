package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public class OdinPsiUtil {
    public static PsiReference getReference(OdinIdentifierExpression self) {
        return new OdinReference(self);
    }

    public static PsiElement getOperator(OdinBinaryExpression self) {
        return self.getChildren().length > 1 ? self.getChildren()[1] : null;
    }
}
