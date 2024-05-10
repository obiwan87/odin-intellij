package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinArrayType extends TsOdinType {
    TsOdinType elementType;

    @Override
    public String getLabel() {
        return "[]" + (elementType != null ? elementType.getLabel() : "<undefined>");
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.ARRAY;
    }
}
