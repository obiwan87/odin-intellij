package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.NotNullLazyValue;
import com.lasagnerd.odin.OdinIcons;

public class OdinRunConfigurationType extends ConfigurationTypeBase {

    public static final String ID = "OdinRunConfiguration";

    public OdinRunConfigurationType() {
        super(ID, "Odin", "Odin run configuration type",
                NotNullLazyValue.createValue(() -> OdinIcons.OdinRunConfiguration));
        addFactory(new OdinRunConfigurationFactory(this));
    }


}
