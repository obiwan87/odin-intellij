package com.lasagnerd.odin.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.RunParameters;
import com.jetbrains.cidr.execution.TrivialInstaller;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriverConfiguration;
import com.lasagnerd.odin.runConfiguration.OdinRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class OdinDebugRunParameters extends RunParameters {

    private final OdinRunConfiguration runProfile;
    GeneralCommandLine commandLine;

	public OdinDebugRunParameters(@NotNull OdinRunConfiguration runProfile, GeneralCommandLine commandLine) {
        this.runProfile = runProfile;
        this.commandLine = commandLine;
	}

	private class OdinInstaller implements Installer {

		@Override
		public @NotNull GeneralCommandLine install() throws ExecutionException {
			return OdinDebugRunParameters.this.commandLine;
		}

		@Override
		public @NotNull File getExecutableFile() {
			return new File(OdinDebugRunParameters.this.runProfile.getOutputPath());
		}
	}

	@Override
	public @NotNull Installer getInstaller() {
		return new TrivialInstaller(this.commandLine);
	}

	@Override
	public @NotNull DebuggerDriverConfiguration getDebuggerDriverConfiguration() {
		return new LLDBDriverConfiguration() {

			@Override
			public @NotNull String getDriverName() {
				return "Odin LLDB";
			}
		};
	}

	@Override
	public @Nullable String getArchitectureId() {
		return "x86_64";
	}
}
