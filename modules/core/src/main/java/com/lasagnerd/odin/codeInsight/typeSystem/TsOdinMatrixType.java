package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinMatrixType extends TsOdinTypeBase implements TsOdinElementOwner {
    private TsOdinType elementType;

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.UNKNOWN;
    }
}
