package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinPointerType extends TsOdinTypeBase {
    private TsOdinType dereferencedType;

    private boolean soa;

    @Override
    public String getLabel() {
        return "^" + (dereferencedType != null ? dereferencedType.getLabel() : "<undefined>");
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.POINTER;
    }
}
