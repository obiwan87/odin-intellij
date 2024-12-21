package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinEnumType extends TsOdinTypeBase {
    TsOdinBuiltInType backingType;

    @Override
    public String getLabel() {
        return "enum " + getName() + labelOrEmpty(backingType);
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.ENUM;
    }
}
