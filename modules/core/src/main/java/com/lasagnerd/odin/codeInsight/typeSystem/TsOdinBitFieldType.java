package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinBitFieldType extends TsOdinTypeBase {
    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.BIT_FIELD;
    }
}
