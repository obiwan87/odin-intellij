package com.lasagnerd.odin.codeInsight.symbols.symbolTable;

import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.lang.psi.OdinScopeBlock;

public interface OdinSymbolTableBuilderListener {
    default boolean onCheckpointCalled(OdinSymbolTable symbolTable) {
        return false;
    }

    default boolean onBlockConsumed(OdinSymbolTable symbolTable, OdinScopeBlock scopeBlock) {
        return false;
    }
}
