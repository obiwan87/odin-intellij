package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinAnyType extends TsOdinBuiltInType {
    TsOdinStructType backingType;

    public TsOdinAnyType(TsOdinStructType backingType) {
        super("any");
        this.backingType = backingType;
    }

    @Override
    public boolean isAnyType() {
        return true;
    }
}
