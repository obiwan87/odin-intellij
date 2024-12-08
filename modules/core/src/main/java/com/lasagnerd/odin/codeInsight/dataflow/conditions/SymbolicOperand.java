package com.lasagnerd.odin.codeInsight.dataflow.conditions;

import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import lombok.Getter;

@Getter
public class SymbolicOperand extends Operand {
    OdinSymbol symbol;

    public SymbolicOperand(OdinSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        if (symbol != null) {
            return symbol.getName();
        }
        return "<undefined>";
    }
}
