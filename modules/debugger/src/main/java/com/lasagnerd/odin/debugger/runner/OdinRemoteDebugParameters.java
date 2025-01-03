package com.lasagnerd.odin.debugger.runner;

import com.intellij.util.system.CpuArch;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.RunParameters;
import com.jetbrains.cidr.execution.TrivialInstaller;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.remote.CidrRemoteDebugParameters;
import com.lasagnerd.odin.debugger.driverConfigurations.LLDBDAPDriverConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinRemoteDebugParameters extends RunParameters {
    CidrRemoteDebugParameters remoteDebugParameters;

    LLDBDAPDriverConfiguration debuggerDriverConfiguration;

    public OdinRemoteDebugParameters(CidrRemoteDebugParameters remoteDebugParameters, LLDBDAPDriverConfiguration debuggerDriverConfiguration) {
        this.remoteDebugParameters = remoteDebugParameters;
        this.debuggerDriverConfiguration = debuggerDriverConfiguration;
    }

    @Override
    public @NotNull Installer getInstaller() {
        return new TrivialInstaller(null);
    }

    @Override
    public @NotNull DebuggerDriverConfiguration getDebuggerDriverConfiguration() {
        return debuggerDriverConfiguration;
    }

    @Override
    public @Nullable String getArchitectureId() {
        return CpuArch.CURRENT.name();
    }
}
