package com.lasagnerd.odin.codeInsight.typeSystem;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType.MetaType.SLICE;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
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
