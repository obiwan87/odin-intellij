package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class OdinPsiElementImpl extends ASTWrapperPsiElement {
    public OdinPsiElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}
