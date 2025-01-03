package com.lasagnerd.odin.debugger.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.lasagnerd.odin.runConfiguration.OdinBaseCommandLineState;
import org.jetbrains.annotations.NotNull;

public class OdinRemoteDebugRunCommandLineState extends OdinBaseCommandLineState {
    protected OdinRemoteDebugRunCommandLineState(ExecutionEnvironment environment) {
        super(environment);
    }

    @Override
    protected @NotNull ProcessHandler startProcess() throws ExecutionException {
        return null;
    }

    @Override
    public @NotNull GeneralCommandLine createCommandLine(boolean debug) {
        return null;
    }
}
