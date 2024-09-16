package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinDynamicArray extends TsOdinType {
    private TsOdinType elementType;
    private boolean soa;

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.DYNAMIC_ARRAY;
    }

    @Override
    public String getLabel() {
        return "[dynamic]" + label(elementType);
    }
}
