package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

public class OdinPsiElementImpl extends ASTWrapperPsiElement {
    public OdinPsiElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        IElementType elementType = PsiUtilCore.getElementType(this);
        if(elementType != null) {
            return elementType.toString();
        }
        return super.toString();
    }
}
