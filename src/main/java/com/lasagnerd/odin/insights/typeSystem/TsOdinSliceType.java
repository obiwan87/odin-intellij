package com.lasagnerd.odin.insights.typeSystem;


import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.lasagnerd.odin.insights.typeSystem.TsOdinMetaType.MetaType.SLICE;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinSliceType extends TsOdinType {
    TsOdinType elementType;

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return SLICE;
    }

    @Override
    public String getLabel() {
        return "[]" + (elementType != null ? elementType.getLabel() : "<undefined>");
    }

}
