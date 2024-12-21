package com.lasagnerd.odin.codeInsight.typeSystem;

public class TsOdinRuneType extends TsOdinBuiltInType{
    TsOdinRuneType() {
        super("rune");
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.RUNE;
    }
}
