package com.lasagnerd.odin.insights.typeSystem;

import com.lasagnerd.odin.lang.psi.OdinFile;

public class TsOdinPackageType extends TsOdinType {

    @Override
    public TsOdinMetaType.MetaType getMetaType() {
        return TsOdinMetaType.MetaType.PACKAGE;
    }
}
