package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinMultiPointerType extends TsOdinTypeBase {
    TsOdinType dereferencedType;

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.MULTI_POINTER;
    }

    @Override
    public String getLabel() {
        return "[^]"+dereferencedType.getLabel();
    }
}
