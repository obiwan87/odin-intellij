package com.lasagnerd.odin.codeInsight.symbols;

import com.lasagnerd.odin.codeInsight.OdinSymbolTable;

@FunctionalInterface
public interface OdinSymbolTableBuilder {
    OdinSymbolTable build();
}
