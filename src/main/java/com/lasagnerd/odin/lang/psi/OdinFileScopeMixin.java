package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.OdinScopeResolver;
import org.jetbrains.annotations.NotNull;

public abstract class OdinFileScopeMixin extends ASTWrapperPsiElement {

    protected OdinSymbolTable symbolTable;

    public OdinFileScopeMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        symbolTable = null;
    }

    public OdinSymbolTable getSymbolTable() {
        if (this instanceof OdinFileScope odinFileScope) {
            if (symbolTable == null) {
                symbolTable = OdinScopeResolver.getFileScopeDeclarations(
                        odinFileScope,
                        OdinScopeResolver.getGlobalFileVisibility(odinFileScope)
                );
            }

            return symbolTable;
        }
        return null;
    }
}
