package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinDynamicArray extends TsOdinType {
    private TsOdinType elementType;
    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.DYNAMIC_ARRAY;
    }
}
