package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;

@FunctionalInterface
public interface OdinSymbolTableProvider {
    OdinSymbolTable build(OdinContext context, PsiElement element);
}
