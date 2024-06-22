package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinBoolType extends TsOdinBuiltInType{
    int length;

    TsOdinBoolType(String name, int length) {
        super(name);
        this.length = length;
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.BOOL;
    }
}
