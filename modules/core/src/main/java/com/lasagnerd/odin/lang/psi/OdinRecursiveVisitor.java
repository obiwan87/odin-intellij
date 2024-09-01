package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import org.jetbrains.annotations.NotNull;

public class OdinRecursiveVisitor extends OdinVisitor implements PsiRecursiveVisitor {
    @Override
    public void visitPsiElement(@NotNull PsiElement o) {
        o.acceptChildren(this);
    }
}
