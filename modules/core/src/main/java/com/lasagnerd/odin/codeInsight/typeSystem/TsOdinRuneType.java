package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinRuneType extends TsOdinBuiltInType{
    TsOdinRuneType() {
        super("rune");
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.RUNE;
    }
}
