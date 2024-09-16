package com.lasagnerd.odin.codeInsight.typeSystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class TsOdinPackageReferenceType extends TsOdinType {

    private final String referencingPackagePath;

    public TsOdinPackageReferenceType(String referencingPackagePath) {
        this.referencingPackagePath = referencingPackagePath;
    }

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.PACKAGE;
    }
}
