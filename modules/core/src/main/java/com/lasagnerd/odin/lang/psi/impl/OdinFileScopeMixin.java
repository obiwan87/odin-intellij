package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableHelper;
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
                symbolTable = OdinSymbolTableHelper.buildFileScopeSymbolTable(
                        odinFileScope,
                        OdinSymbolTableHelper.getGlobalFileVisibility(odinFileScope)
                );
            }

            return symbolTable;
        }
        return null;
    }
}
