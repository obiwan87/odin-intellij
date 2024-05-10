package com.lasagnerd.odin.insights.typeSystem;

public class TsOdinConstrainedType extends TsOdinType {
    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.UNKNOWN;
    }
}
