package com.lasagnerd.odin.codeInsight.typeSystem;


import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType.MetaType.SLICE;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinSliceType extends TsOdinType {
    TsOdinType elementType;
    boolean soa;

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return SLICE;
    }

    @Override
    public String getLabel() {
        return "[]" + (elementType != null ? elementType.getLabel() : "<undefined>");
    }

}
