package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.icons.AllIcons;
import com.intellij.icons.ExpUiIcons;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import com.lasagnerd.odin.lang.psi.impl.OdinStatementImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class OdinIdentifierOwner extends ASTWrapperPsiElement implements OdinDeclaredIdentifier, PsiNameIdentifierOwner {
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
        return getIdentifierToken().getText();
    }

    public void accept(@NotNull OdinVisitor visitor) {

    }

    @Override
    public @NotNull SearchScope getUseScope() {
        PsiElement scopeBlock = OdinInsightUtils.findFirstParentOfType(this, true, OdinScopeBlock.class);
        if (scopeBlock != null) {
            if(scopeBlock instanceof OdinIfBlock || scopeBlock instanceof OdinElseBlock || scopeBlock instanceof OdinElseIfBlock) {
                return new LocalSearchScope(OdinInsightUtils.findFirstParentOfType(scopeBlock, true, OdinConditionalStatement.class));
            }
            return new LocalSearchScope(scopeBlock);
        }
        return super.getUseScope();
    }
}
