package com.lasagnerd.odin.codeInsight.typeSystem;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinTypeKind.SLICE;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinSliceType extends TsOdinTypeBase implements TsOdinElementOwner {
    TsOdinType elementType;
    boolean soa;

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return SLICE;
    }

    @Override
    public String getLabel() {
        return "[]" + (elementType != null ? elementType.getLabel() : "<undefined>");
    }

}
