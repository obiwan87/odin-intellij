package com.lasagnerd.odin.debugger;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.psi.search.ExecutionSearchScopes;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.lasagnerd.odin.runConfiguration.OdinRunCommandLineState;
import org.jetbrains.annotations.NotNull;

public class OdinNativeDebugProgramRunner extends GenericProgramRunner<RunnerSettings> {

    @Override
    public @NotNull String getRunnerId() {
        return "OdinNativeDebugProgramRunner";
    }


    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId); // && profile instanceof OdinRunConfiguration // needed ? will this be called for non odin files/projects?
    }


    @Override
    protected void execute(@NotNull ExecutionEnvironment environment, @NotNull RunProfileState state) {
        // The state is passed through OdinRunCommandLineState which is provided OdinRunConfiguration
        if (state instanceof OdinRunCommandLineState odinRunCommandLine) {
            try {
                TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(environment.getProject(), ExecutionSearchScopes.executionScope(environment.getProject(), environment.getRunProfile()));
                odinRunCommandLine.setDebug(true);
                OdinDebugRunParameters runParameters = new OdinDebugRunParameters(odinRunCommandLine.createCommandLine());
                XDebuggerManager debuggerManager = XDebuggerManager.getInstance(environment.getProject());
                debuggerManager.startSessionAndShowTab("Odin Debugger", null, new XDebugProcessStarter() {

                    @Override
                    public @NotNull XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
                        OdinLocalDebugProcess debugProcess = new OdinLocalDebugProcess(runParameters, session, consoleBuilder);
                        ProcessTerminatedListener.attach(debugProcess.getProcessHandler(), environment.getProject());
                        debugProcess.start();
                        return debugProcess;
                    }
                }).getRunContentDescriptor();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}