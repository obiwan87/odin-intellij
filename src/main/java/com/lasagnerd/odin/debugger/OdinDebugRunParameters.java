package com.lasagnerd.odin.debugger;

import com.google.protobuf.Message;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.util.Key;
import com.jetbrains.cidr.ArchitectureType;
import com.intellij.util.system.CpuArch;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.RunParameters;
import com.jetbrains.cidr.execution.TrivialInstaller;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriver;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriverConfiguration;
import com.lasagnerd.odin.debugger.dap.lldb.LldbDapDriverConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinDebugRunParameters extends RunParameters {

    GeneralCommandLine commandLine;

	public OdinDebugRunParameters(GeneralCommandLine commandLine) {
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
		return CpuArch.CURRENT.name();
	}

	private static class MyLLDBDriver extends LLDBDriver {

		public MyLLDBDriver(@NotNull Handler handler, @NotNull LLDBDriverConfiguration starter, @NotNull ArchitectureType architectureType) throws ExecutionException {
			super(handler, starter, architectureType);
			getProcessHandler().addProcessListener(new ProcessListener() {
				@Override
				public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
					System.out.println("LLDB says: " + event.getText());
				}

				@Override
				public void processTerminated(@NotNull ProcessEvent event) {
					System.out.println("Process terminated with code " + event.getExitCode());
				}
			});
		}

		@Override
		public <R extends Message, E extends Exception> @NotNull R sendMessageAndWaitForReply(@NotNull Message message, @NotNull Class<R> responseClass, @NotNull ResponseMessageConsumer<? super R, E> errorHandler, long msTimeout) throws ExecutionException, E {
			System.out.println("Sending message: " + message);
			R r = super.sendMessageAndWaitForReply(message, responseClass, errorHandler, msTimeout);
			System.out.println("Response: " + r);
			return r;
		}
	}

	private static class MyLLDBDriverConfiguration extends LLDBDriverConfiguration {
		@Override
		public @NotNull GeneralCommandLine createDriverCommandLine(@NotNull DebuggerDriver driver, @NotNull ArchitectureType architectureType) throws ExecutionException {
			GeneralCommandLine driverCommandLine = super.createDriverCommandLine(driver, architectureType);
			driverCommandLine.withEnvironment("LLDB_USE_NATIVE_PDB_READER", "yes");
			return driverCommandLine;
		}

		@Override
		public @NotNull LLDBDriver createDriver(DebuggerDriver.@NotNull Handler handler, @NotNull ArchitectureType architectureType) throws ExecutionException {
			return new MyLLDBDriver(handler, this, architectureType);
		}
	}
}
