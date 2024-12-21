package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinPolymorphicType extends TsOdinTypeBase {
    boolean explicit;

    @Override
    public String getLabel() {
        return "$" + super.getLabel();
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.POLYMORPHIC;
    }

}
