package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.symbols.OdinContextBuilder;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import org.jetbrains.annotations.NotNull;

public abstract class OdinFileScopeMixin extends OdinPsiElementImpl {

    protected OdinContext context;

    public OdinFileScopeMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        context = null;
    }

    public OdinContext getFullContext() {
        if (this instanceof OdinFileScope odinFileScope) {
            if (context == null) {
                context = OdinContextBuilder.buildFileScopeContext(
                        odinFileScope,
                        OdinContextBuilder.getGlobalFileVisibility(odinFileScope)
                );
            }

            return context;
        }
        return null;
    }
}
