package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;

public interface OdinPsiElement extends PsiElement {
    /**
     * Unwraps parentheses and returns inner expression recursively.
     * (((expr))) -> expr
     * @return The unwrapped expression
     */
    OdinExpression parenthesesUnwrap();

    String getLocation();

    OdinFile getContainingOdinFile();
}
