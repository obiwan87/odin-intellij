package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinPackageReferenceType extends TsOdinTypeBase {

    private final String referencingPackagePath;

    public TsOdinPackageReferenceType(String referencingPackagePath) {
        this.referencingPackagePath = referencingPackagePath;
    }

    @Override
    public TsOdinTypeKind getTypeReferenceKind() {
        return TsOdinTypeKind.PACKAGE;
    }
}
