package com.lasagnerd.odin.debugger.runner;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.util.system.CpuArch;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.RunParameters;
import com.jetbrains.cidr.execution.TrivialInstaller;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinDebugRunParameters extends RunParameters {

    GeneralCommandLine commandLine;
    private final DebuggerDriverConfiguration debuggerDriverConfiguration;

    public OdinDebugRunParameters(GeneralCommandLine commandLine, DebuggerDriverConfiguration debuggerDriverConfiguration) {
        this.commandLine = commandLine;
        this.debuggerDriverConfiguration = debuggerDriverConfiguration;
    }

	@Override
	public @NotNull Installer getInstaller() {
		return new TrivialInstaller(this.commandLine);
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
