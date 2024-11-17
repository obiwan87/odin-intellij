package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinBitFieldType extends TsOdinTypeBase {
    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.BIT_FIELD;
    }
}
