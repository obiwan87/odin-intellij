package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinUntypedType extends TsOdinBuiltInType {
    private final TsOdinTypeKind typeReferenceKind;

    TsOdinUntypedType(String name, TsOdinTypeKind typeReferenceKind) {
        super(name);
        this.typeReferenceKind = typeReferenceKind;
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return this.typeReferenceKind;
    }
}
