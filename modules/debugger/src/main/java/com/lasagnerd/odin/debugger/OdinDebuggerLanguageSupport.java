package com.lasagnerd.odin.debugger;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerEditorsProvider;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerLanguageSupport;
import com.jetbrains.cidr.execution.debugger.CidrEvaluator;
import com.jetbrains.cidr.execution.debugger.CidrStackFrame;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class OdinDebuggerLanguageSupport extends CidrDebuggerLanguageSupport {

	@Override
	public @NotNull Set<DebuggerDriver.DebuggerLanguage> getSupportedDebuggerLanguages() {
		return Set.of(DebuggerDriver.StandardDebuggerLanguage.C);
	}

	@Override
	public @Nullable XDebuggerEditorsProvider createEditor(@Nullable RunProfile profile) {
		return new CidrDebuggerEditorsProvider();
	}

	@Override
	protected @Nullable CidrEvaluator createEvaluator(@NotNull CidrStackFrame frame) {
		return new OdinDebuggerEvaluator(frame);
	}


}
