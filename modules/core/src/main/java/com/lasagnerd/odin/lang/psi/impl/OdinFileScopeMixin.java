package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import org.jetbrains.annotations.NotNull;

public abstract class OdinFileScopeMixin extends OdinPsiElementImpl {

    protected OdinSymbolTable symbolTable;

    public OdinFileScopeMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        symbolTable = null;
    }

    public OdinSymbolTable getFullSymbolTable() {
        if (this instanceof OdinFileScope odinFileScope) {
            if (symbolTable == null) {
                symbolTable = OdinSymbolTableResolver.getFileScopeSymbols(
                        odinFileScope,
                        OdinSymbolTableResolver.getGlobalFileVisibility(odinFileScope)
                );
            }

            return symbolTable;
        }
        return null;
    }
}
