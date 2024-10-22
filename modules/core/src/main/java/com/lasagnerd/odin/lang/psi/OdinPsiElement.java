package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;

public interface OdinPsiElement extends PsiElement {
    OdinSymbolTable getSymbolTable();
    void setSymbolTable(OdinSymbolTable symbolTable);
}
