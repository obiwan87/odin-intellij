package com.lasagnerd.odin.debugger.runConfiguration;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;

public class OdinRemoteDebugRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "OdinRemoteDebugRunConfiguration";

    public OdinRemoteDebugRunConfigurationType() {
        super(ID, "Odin Remote LLDB Server", "Remote debug over lldb", AllIcons.RunConfigurations.RemoteDebug);
        addFactory(new OdinRemoteDebugRunConfigurationFactory(this));
    }
}
