package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class OdinPsiElement extends ASTWrapperPsiElement {
    public OdinPsiElement(@NotNull ASTNode node) {
        super(node);
    }
}
