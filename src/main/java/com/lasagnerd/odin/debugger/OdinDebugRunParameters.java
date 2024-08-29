package com.lasagnerd.odin.debugger;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.RunParameters;
import com.jetbrains.cidr.execution.TrivialInstaller;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.dap.lldb.LldbDapDriverConfiguration;
import com.lasagnerd.odin.runConfiguration.OdinRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinDebugRunParameters extends RunParameters {

    private final OdinRunConfiguration runProfile;
    GeneralCommandLine commandLine;

	public OdinDebugRunParameters(@NotNull OdinRunConfiguration runProfile, GeneralCommandLine commandLine) {
        this.runProfile = runProfile;
        this.commandLine = commandLine;
	}

	@Override
	public @NotNull Installer getInstaller() {
		return new TrivialInstaller(this.commandLine);
	}

	@Override
	public @NotNull DebuggerDriverConfiguration getDebuggerDriverConfiguration() {
		return new LldbDapDriverConfiguration();
	}

	@Override
	public @Nullable String getArchitectureId() {
		return "x86_64";
	}
}
