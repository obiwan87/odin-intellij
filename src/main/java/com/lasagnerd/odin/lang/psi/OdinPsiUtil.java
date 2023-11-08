package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;

public class OdinPsiUtil {
    public static PsiReference getReference(OdinIdentifier self) {
        return new OdinReference(self);
    }

    public static PsiElement getOperator(OdinBinaryExpression self) {
        return self.getChildren().length > 1 ? self.getChildren()[1] : null;
    }

    public static OdinCompoundValueBody getCompoundValueBody(OdinCompoundValue self) {
        return PsiTreeUtil.findChildOfType(self, OdinCompoundValueBody.class);
    }
}
