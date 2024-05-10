package com.lasagnerd.odin.insights.typeSystem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TsOdinBitSetType extends TsOdinType {
    TsOdinType elementType;
    TsOdinType backingType;

    @Override
    public String getLabel() {
        if (backingType == null) {
            return "bit_set[" + elementType.getLabel() + "]";
        }
        return "bit_set[" + elementType.getLabel() + "; " + backingType.getLabel() + "]";
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.BIT_SET;
    }
}
