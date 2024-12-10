package com.lasagnerd.odin.codeInsight.dataflow;

import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.dataflow.conditions.Condition;
import com.lasagnerd.odin.codeInsight.dataflow.constraints.OdinConstraint;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValue;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class OdinLattice {

    public static final OdinLattice EMPTY = new OdinLattice(OdinContext.EMPTY);
    // Facts
    OdinSymbolValueStore symbolValueStore = new OdinSymbolValueStore();
    List<OdinConstraint> constraints = new ArrayList<>();
    List<Condition> conditions = new ArrayList<>();
    final OdinContext context;

    public OdinLattice() {
        this(OdinContext.EMPTY);
    }

    public static OdinLattice fromContext(OdinContext context) {
        if (context == null)
            context = OdinContext.EMPTY;
        OdinLattice lattice = new OdinLattice(context);
        lattice.symbolValueStore = context.getSymbolValueStore().copy();
        return lattice;
    }

    public OdinLattice(OdinContext context) {
        this.context = context;
    }

    // TODO implement
    public OdinLattice copy() {
        OdinLattice lattice = new OdinLattice(context);
        lattice.symbolValueStore = lattice.getSymbolValueStore().copy();
        lattice.getConditions().addAll(conditions);
        lattice.getConstraints().addAll(constraints);

        return lattice;
    }

    public OdinContext getContext() {
        return this.context.withSymbolValueStore(symbolValueStore);
    }

    public void combine(OdinLattice lattice) {
        this.symbolValueStore.combine(lattice.symbolValueStore);
    }

    public void intersect(OdinLattice lattice) {
        this.symbolValueStore.intersect(lattice.symbolValueStore);
    }

    public OdinLattice intersected(OdinLattice lattice) {
        OdinLattice copy = this.copy();
        copy.intersect(lattice);
        return lattice;
    }

    public boolean isSubset(OdinLattice lattice) {
        return this.symbolValueStore.isSubset(lattice.symbolValueStore);
    }

    public void printValues() {
        symbolValueStore.printValues();
    }

    public Map<OdinSymbol, EvOdinValue> getValues() {
        return symbolValueStore.getValues();
    }
}
