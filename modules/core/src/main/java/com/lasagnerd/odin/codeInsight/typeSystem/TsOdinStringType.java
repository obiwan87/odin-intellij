package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinStringType extends TsOdinBuiltInType {
    TsOdinStringType(String name) {
        super(name);
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.STRING;
    }
}
