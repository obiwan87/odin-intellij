package com.lasagnerd.odin.debugger;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.NlsSafe;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.RunParameters;
import com.jetbrains.cidr.execution.TrivialInstaller;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.gdb.GDBDriverConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinDebugRunParameters extends RunParameters {

	GeneralCommandLine commandLine;

	public OdinDebugRunParameters(GeneralCommandLine commandLine) {
		this.commandLine = commandLine;
	}

	@Override
	public @NotNull Installer getInstaller() {
		return new TrivialInstaller(commandLine);
	}

	@Override
	public @NotNull DebuggerDriverConfiguration getDebuggerDriverConfiguration() {
		return new GDBDriverConfiguration() {
			@Override
			public @NotNull @NlsSafe String getDriverName() {
				return "Odin GDB";
			}
		};
	}

	@Override
	public @Nullable String getArchitectureId() {
		return "";
	}
}
