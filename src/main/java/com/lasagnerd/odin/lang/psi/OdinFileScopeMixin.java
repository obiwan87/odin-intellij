package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.lasagnerd.odin.codeInsight.OdinScope;
import com.lasagnerd.odin.codeInsight.OdinScopeResolver;
import org.jetbrains.annotations.NotNull;

public abstract class OdinFileScopeMixin extends ASTWrapperPsiElement {

    protected OdinScope scope;

    public OdinFileScopeMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        scope = null;
    }

    public OdinScope getScope() {
        if (this instanceof OdinFileScope odinFileScope) {
            if (scope == null) {
                scope = OdinScopeResolver.getFileScopeDeclarations(
                        odinFileScope,
                        OdinScopeResolver.getGlobalFileVisibility(odinFileScope)
                );
            }

            return scope;
        }
        return null;
    }
}