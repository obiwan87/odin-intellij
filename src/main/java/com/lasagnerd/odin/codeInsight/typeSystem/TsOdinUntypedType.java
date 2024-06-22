package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinUntypedType extends TsOdinBuiltInType {
    private final TsOdinMetaType.MetaType metaType;

    TsOdinUntypedType(String name, TsOdinMetaType.MetaType metaType) {
        super(name);
        this.metaType = metaType;
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return this.metaType;
    }
}
