package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
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

public class OdinIdentifierOwner extends ASTWrapperPsiElement
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
        OdinDeclaredIdentifier declaredIdentifier = OdinPsiElementFactory.getInstance(getProject()).createDeclaredIdentifier(name);
        ASTNode currentIdentifierToken = getNode().findChildByType(OdinTypes.IDENTIFIER_TOKEN);
        ASTNode newIdentifierToken = declaredIdentifier.getNode().findChildByType(OdinTypes.IDENTIFIER_TOKEN);
        if (currentIdentifierToken != null && newIdentifierToken != null) {
            getNode().replaceChild(currentIdentifierToken, newIdentifierToken);
        }
        return this;
    }

    @Override
    public String getName() {
        return super.getText();
    }

    public void accept(@NotNull OdinVisitor visitor) {

    }
}
