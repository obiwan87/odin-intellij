package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Getter;

public class TsOdinAnyType extends TsOdinBuiltInType {
    @Getter
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
