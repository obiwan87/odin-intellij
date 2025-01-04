package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TsOdinObjcClass extends TsOdinTypeBase {
    TsOdinStructType structType;
    String objcClassName;

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.OBJC_CLASS;
    }
}
