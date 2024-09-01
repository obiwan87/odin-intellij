package com.lasagnerd.odin.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.cidr.execution.RunParameters;
import com.jetbrains.cidr.execution.debugger.CidrLocalDebugProcess;
import org.jetbrains.annotations.NotNull;

public class OdinLocalDebugProcess extends CidrLocalDebugProcess {

	public OdinLocalDebugProcess(@NotNull RunParameters parameters, @NotNull XDebugSession session, @NotNull TextConsoleBuilder consoleBuilder) throws ExecutionException {
		super(parameters, session, consoleBuilder, (project) -> Filter.EMPTY_ARRAY, false);
	}

	@Override
	public boolean isLibraryFrameFilterSupported() {
		return false;
	}

}