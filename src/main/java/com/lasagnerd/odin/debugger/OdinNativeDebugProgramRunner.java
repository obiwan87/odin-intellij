package com.lasagnerd.odin.debugger;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.util.Key;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.cidr.execution.CidrRunner;
import com.lasagnerd.odin.runConfiguration.OdinRunCommandLineState;
import com.lasagnerd.odin.runConfiguration.OdinRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinNativeDebugProgramRunner extends CidrRunner {


    @Override
    public @NotNull String getRunnerId() {
        return "OdinNativeDebugProgramRunner";
    }


    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)  && profile instanceof OdinRunConfiguration;
    }


    @Override
    protected @Nullable RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        // The state is passed through OdinRunCommandLineState which is provided OdinRunConfiguration
        if (!(state instanceof OdinRunCommandLineState odinRunCommandLine) || !(environment.getRunProfile() instanceof OdinRunConfiguration odinRunConfiguration)) {
            return null;
        }

        GeneralCommandLine runExecutable = new GeneralCommandLine(odinRunConfiguration.getOutputPath());
        OdinDebugRunParameters runParameters = new OdinDebugRunParameters(odinRunConfiguration, runExecutable);
        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(environment.getProject());
        XDebugSession xDebugSession = debuggerManager.startSession(environment, new XDebugProcessStarter() {
            @Override
            public @NotNull XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {

                OdinLocalDebugProcess debugProcess = new OdinLocalDebugProcess(runParameters, session, odinRunCommandLine.getConsoleBuilder());
                ConsoleView console = debugProcess.getConsole();
                OSProcessHandler processHandler = ProcessHandlerFactory.getInstance().createProcessHandler(
                        odinRunCommandLine.createCommandLine(true));
                console.attachToProcess(processHandler);

                BuildProcessListener failedToBuild = new BuildProcessListener(debugProcess, environment, console);

                processHandler.addProcessListener(failedToBuild);
                processHandler.startNotify();
                processHandler.waitFor();
                if(!failedToBuild.buildSuccess) {
                    console.print("Build failed. Starting debug session with previously built executable if any exists. \n", ConsoleViewContentType.ERROR_OUTPUT);
                } else {
                    console.print("Build Successful. Starting debug session. \n", ConsoleViewContentType.ERROR_OUTPUT);
                }
                debugProcess.start();
                return debugProcess;
            }
        });

        return xDebugSession.getRunContentDescriptor();
    }

    private static class BuildProcessListener implements ProcessListener {
        private final OdinLocalDebugProcess debugProcess;
        private final @NotNull ExecutionEnvironment environment;
        private boolean buildSuccess;

        public BuildProcessListener(OdinLocalDebugProcess debugProcess, @NotNull ExecutionEnvironment environment, ConsoleView console) {
            this.debugProcess = debugProcess;
            this.environment = environment;
        }

        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
            if (event.getExitCode() == 0) {
                ProcessTerminatedListener.attach(debugProcess.getProcessHandler(), environment.getProject());
                buildSuccess = true;
            } else {
                buildSuccess = false;
            }
        }
    }
}