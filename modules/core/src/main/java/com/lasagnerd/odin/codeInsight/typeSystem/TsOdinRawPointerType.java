package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinRawPointerType extends TsOdinBuiltInType{
    TsOdinRawPointerType() {
        super("rawptr");
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.RAWPTR;
    }
}
