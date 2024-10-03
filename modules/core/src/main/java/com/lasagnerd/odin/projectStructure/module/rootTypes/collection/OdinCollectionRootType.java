package com.lasagnerd.odin.projectStructure.module.rootTypes.collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

public class OdinCollectionRootType
        extends JpsElementTypeBase<OdinCollectionRootProperties>
        implements JpsModuleSourceRootType<OdinCollectionRootProperties> {

    public static final OdinCollectionRootType INSTANCE = new OdinCollectionRootType();
    public static final String ID = "OdinCollectionRootType";

    @Override
    public @NotNull OdinCollectionRootProperties createDefaultProperties() {
        return new OdinCollectionRootProperties();
    }
}
