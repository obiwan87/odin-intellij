package com.lasagnerd.odin.codeInsight.symbols.symbolTable;

import com.lasagnerd.odin.codeInsight.OdinSymbolTable;

@FunctionalInterface
public interface OdinSymbolTableBuilder {
    OdinSymbolTable build();
}
