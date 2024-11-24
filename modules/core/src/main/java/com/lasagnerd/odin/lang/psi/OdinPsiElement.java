package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;

public interface OdinPsiElement extends PsiElement {
    OdinSymbolTable getFullSymbolTable();

    void setFullSymbolTable(OdinSymbolTable fullSymbolTable);

    /**
     * Unwraps parentheses and returns inner expression recursively.
     * (((expr))) -> expr
     * @return The unwrapped expression
     */
    OdinExpression parenthesesUnwrap();
}
