package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinBitSetType extends TsOdinType {
    TsOdinType elementType;
    TsOdinType backingType;

    @Override
    public String getLabel() {
        if (backingType == null) {
            return "bit_set[" + label(elementType) + "]";
        }
        return "bit_set[" + label(elementType) + "; " + label(backingType) + "]";
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.BIT_SET;
    }
}
