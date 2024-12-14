package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;

public interface OdinPsiElement extends PsiElement {
    OdinSymbolTable getFullSymbolTable();

    void setFullSymbolTable(OdinSymbolTable symbolTable);

    /**
     * Unwraps parentheses and returns inner expression recursively.
     * (((expr))) -> expr
     * @return The unwrapped expression
     */
    OdinExpression parenthesesUnwrap();

    String getLocation();
}
