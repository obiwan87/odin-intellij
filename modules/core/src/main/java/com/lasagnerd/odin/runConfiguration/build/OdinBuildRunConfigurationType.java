package com.lasagnerd.odin.runConfiguration.build;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.NotNullLazyValue;
import com.lasagnerd.odin.OdinIcons;

public class OdinBuildRunConfigurationType extends ConfigurationTypeBase {

    public static final String ID = "OdinRunConfiguration";

    public OdinBuildRunConfigurationType() {
        super(ID, "Odin Build", "Builds and runs an Odin application using the odin tool.",
                NotNullLazyValue.createValue(() -> OdinIcons.OdinRunConfiguration));
        addFactory(new OdinBuildRunConfigurationFactory(this));
    }


}
