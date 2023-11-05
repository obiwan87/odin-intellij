package com.lasagnerd.odin.lang.psi;

import com.intellij.icons.AllIcons;
import com.intellij.icons.ExpUiIcons;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.impl.OdinStatementImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdinIdentifierOwner extends OdinStatementImpl
        implements PsiNameIdentifierOwner {
    public OdinIdentifierOwner(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return this;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {

        return this;
    }

    @Override
    public String getName() {
        return super.getText();
    }
}
