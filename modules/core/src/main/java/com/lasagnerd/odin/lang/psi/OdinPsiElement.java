package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinContext;

public interface OdinPsiElement extends PsiElement {
    OdinContext getFullContext();

    void setFullContext(OdinContext fullContext);

    /**
     * Unwraps parentheses and returns inner expression recursively.
     * (((expr))) -> expr
     * @return The unwrapped expression
     */
    OdinExpression parenthesesUnwrap();
}
