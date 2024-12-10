package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinContext;

@FunctionalInterface
public interface OdinContextProvider {
    public OdinContext build(OdinContext context, PsiElement element);
}
