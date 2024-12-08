package com.lasagnerd.odin.codeInsight.dataflow.constraints;

import com.lasagnerd.odin.codeInsight.dataflow.OdinLattice;

@FunctionalInterface
public interface OdinTransferFunction {
    OdinLattice transfer(OdinLattice lattice);
}
