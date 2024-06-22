package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinStringType extends TsOdinBuiltInType {
    TsOdinStringType(String name) {
        super(name);
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.STRING;
    }
}
