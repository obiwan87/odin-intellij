package com.lasagnerd.odin.codeInsight.dataflow.constraints;

import com.lasagnerd.odin.codeInsight.dataflow.OdinLattice;
import com.lasagnerd.odin.codeInsight.dataflow.conditions.AtomicCondition;
import com.lasagnerd.odin.codeInsight.dataflow.conditions.SymbolicOperand;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValueSet;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.lang.psi.OdinTypes;

import java.util.List;

public class OdinSymbolicEqualityConstraint implements OdinTransferFunction {

    private final List<OdinSymbol> symbols;

    private OdinSymbolicEqualityConstraint(List<OdinSymbol> symbols) {
        this.symbols = symbols;

    }

    public static OdinSymbolicEqualityConstraint createOdinEqualityConstraint(AtomicCondition condition) {
        if (!condition.hasOnlySymbols()) {
            throw new IllegalArgumentException("condition must only consist of symbols");
        }

        if (condition.getOperator() != OdinTypes.EQEQ) {
            throw new IllegalArgumentException("Operator must be '=='");
        }
        return new OdinSymbolicEqualityConstraint(condition.getSymbolicOperands().stream().map(SymbolicOperand::getSymbol).toList());
    }


    @Override
    public OdinLattice transfer(OdinLattice lattice) {
        OdinLattice outLattice = lattice.copy();
        EvOdinValueSet valueSet = null;
        for (OdinSymbol symbol : symbols) {
            if (valueSet == null) {
                EvOdinValue value = lattice.getSymbolValueStore().getValue(symbol);
                if (value != null) {
                    valueSet = value.asSet();
                }
            } else {
                EvOdinValueSet currentValue = lattice.getSymbolValueStore().getValue(symbol).asSet();
                valueSet = valueSet.intersect(currentValue);
            }
        }

        if (valueSet != null) {
            for (OdinSymbol symbol : symbols) {
                outLattice.getValues().put(symbol, valueSet);
            }
        }
        return outLattice;
    }
}
