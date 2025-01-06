package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.extapi.psi.ASTDelegatePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.PsiManagerEx;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinSelfArgument;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;

public class OdinSelfArgumentImpl extends ASTDelegatePsiElement implements OdinSelfArgument {
    @Delegate
    private final OdinExpression odinExpression;

    public OdinSelfArgumentImpl(OdinExpression odinExpression) {
        this.odinExpression = odinExpression;
    }

    @Override
    public @NotNull ASTNode getNode() {
        if (odinExpression instanceof OdinExpressionImpl odinExpressionImpl)
            return odinExpressionImpl.getNode();
        throw new NullPointerException("AST node is null");
    }

    @Override
    public OdinExpression getExpression() {
        return odinExpression;
    }

    @Override
    public PsiManagerEx getManager() {
        return (PsiManagerEx) odinExpression.getManager();
    }
}
