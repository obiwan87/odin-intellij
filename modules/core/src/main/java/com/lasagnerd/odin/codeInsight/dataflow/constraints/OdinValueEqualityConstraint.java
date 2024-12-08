package com.lasagnerd.odin.codeInsight.dataflow.constraints;

import com.lasagnerd.odin.codeInsight.dataflow.OdinLattice;
import com.lasagnerd.odin.codeInsight.dataflow.conditions.AtomicCondition;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValueSet;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.lang.psi.OdinTypes;

public class OdinValueEqualityConstraint implements OdinConstraint {
    private final OdinSymbol symbol;
    private final EvOdinValue value;

    public OdinValueEqualityConstraint(OdinSymbol symbol, EvOdinValue value) {
        this.symbol = symbol;
        this.value = value;
    }

    public static OdinValueEqualityConstraint create(AtomicCondition condition) {
        if (condition.hasOnlyValues() || condition.hasOnlySymbols()) {
            throw new IllegalArgumentException("Symbol-value pair expected!");
        }

        if (condition.getOperator() != OdinTypes.EQEQ) {
            throw new IllegalArgumentException("Equality operator expected!");
        }

        return new OdinValueEqualityConstraint(condition.getFirstSymbolicOperand().getSymbol(), condition.getFirstValueOperand().getValue());
    }

    // TODO transfer function should return result object. So we can also return whether there have been
    //  changes -> convergence!
    @Override
    public OdinLattice transfer(OdinLattice lattice) {
        OdinLattice outLattice = lattice.copy();
        transform(lattice);
        // TODO this should probably be a copy of the input lattice
        return outLattice;
    }

    public void transform(OdinLattice lattice) {
        EvOdinValue presentValue = lattice.getValues().computeIfAbsent(symbol, s -> value);
        if (presentValue != value) {
            EvOdinValueSet newValue = presentValue.asSet().intersect(value.asSet());
            lattice.getValues().put(symbol, newValue);
        }
    }
}
