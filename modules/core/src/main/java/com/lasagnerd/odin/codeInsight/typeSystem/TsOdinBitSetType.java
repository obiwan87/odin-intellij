package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinBitSetType extends TsOdinTypeBase implements TsOdinElementOwner {
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
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.BIT_SET;
    }
}
