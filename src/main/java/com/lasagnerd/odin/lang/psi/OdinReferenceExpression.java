package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class OdinReferenceExpression extends PsiReferenceBase<OdinPrimary> {
    public OdinReferenceExpression(@NotNull OdinPrimary element) {
        super(element);
    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return super.getAbsoluteRange();
    }

    @Override
    public @Nullable PsiElement resolve() {
        OdinBlock containingBlock = (OdinBlock) PsiTreeUtil.findFirstParent(getElement(), parent -> parent instanceof OdinBlock);
        if (containingBlock == null) return null;
        OdinStatementList statementList = containingBlock.getStatementList();
        if (statementList == null) return null;
        for (PsiElement child : statementList.getStatementList()) {
            List<OdinIdentifierExpression> declarations = getDeclarations(child);
            for (OdinIdentifierExpression identifier : declarations) {
                if (identifier.getText().equals(getElement().getText())) {
                    return child;
                }
            }
        }
        return null;
    }

    @NotNull
    private static List<OdinIdentifierExpression> getDeclarations(PsiElement child) {
        List<OdinIdentifierExpression> identifierList;
        if (child instanceof OdinVariableDeclarationStatement variableDeclaration) {
            identifierList = variableDeclaration.getIdentifierList().getIdentifierExpressionList();
        } else if (child instanceof OdinVariableInitializationStatement variableInitialization) {
            identifierList = variableInitialization.getIdentifierList().getIdentifierExpressionList();
        } else {
            identifierList = Collections.emptyList();
        }
        return identifierList;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return super.getVariants();
    }
}
