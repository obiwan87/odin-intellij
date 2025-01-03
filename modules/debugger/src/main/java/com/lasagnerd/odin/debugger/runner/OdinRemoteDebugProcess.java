package com.lasagnerd.odin.debugger.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.remote.CidrRemoteDebugParameters;
import com.jetbrains.cidr.execution.debugger.remote.CidrRemotePathMapping;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class OdinRemoteDebugProcess extends CidrDebugProcess {
    public OdinRemoteDebugProcess(@NotNull OdinRemoteDebugParameters parameters,
                                  @NotNull XDebugSession session,
                                  @NotNull TextConsoleBuilder consoleBuilder) throws ExecutionException {
        super(parameters, session, consoleBuilder);
    }

    @Override
    public @NotNull OdinRemoteDebugParameters getRunParameters() {
        return (OdinRemoteDebugParameters) super.getRunParameters();
    }

    @Override
    protected @NotNull DebuggerDriver.Inferior doLoadTarget(@NotNull DebuggerDriver debuggerDriver) throws ExecutionException {
        OdinRemoteDebugParameters runParameters = getRunParameters();
        CidrRemoteDebugParameters remoteDebugParameters = runParameters.remoteDebugParameters;
        List<CidrRemotePathMapping> cidrRemotePathMappings = remoteDebugParameters.getPathMappings();
        List<DebuggerDriver.PathMapping> pathMappings = cidrRemotePathMappings.stream().map(p -> new DebuggerDriver.PathMapping(p.getLocal(), p.getRemote())).toList();

        return debuggerDriver.loadForRemote(
                remoteDebugParameters.expandRemoteCommand(getProject()),
                new File(remoteDebugParameters.expandSymbolFile(getProject())),
                new File(remoteDebugParameters.expandSysroot(getProject())),
                pathMappings
        );
    }

    @Override
    public boolean isLibraryFrameFilterSupported() {
        return false;
    }
}
