package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinRawPointerType extends TsOdinBuiltInType{
    TsOdinRawPointerType() {
        super("rawptr");
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.RAWPTR;
    }
}
