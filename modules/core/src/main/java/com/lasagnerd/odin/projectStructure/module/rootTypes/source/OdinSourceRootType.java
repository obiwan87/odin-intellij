package com.lasagnerd.odin.projectStructure.module.rootTypes.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

public class OdinSourceRootType
        extends JpsElementTypeBase<OdinSourceRootProperties>
        implements JpsModuleSourceRootType<OdinSourceRootProperties> {

    public static final OdinSourceRootType INSTANCE = new OdinSourceRootType();
    public static final String ID = "OdinSourceRootType";

    @Override
    public @NotNull OdinSourceRootProperties createDefaultProperties() {
        return new OdinSourceRootProperties();
    }

}
