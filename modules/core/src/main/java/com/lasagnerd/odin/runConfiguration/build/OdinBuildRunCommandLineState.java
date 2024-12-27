package com.lasagnerd.odin.runConfiguration.build;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class OdinBuildRunCommandLineState extends CommandLineState {
    private final OdinBuildRunConfigurationOptions options;

    public OdinBuildRunCommandLineState(@NotNull ExecutionEnvironment environment,
                                        @NotNull OdinBuildRunConfigurationOptions options) {
        super(environment);
        this.options = options;
    }

    @Override
    protected @NotNull ProcessHandler startProcess() throws ExecutionException {
        addMyConsoleFilters();
        boolean debug = getEnvironment().getExecutor().getId().equals(DefaultDebugExecutor.EXECUTOR_ID);

        GeneralCommandLine commandLine = createCommandLine(debug);

        OSProcessHandler processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);
        ProcessTerminatedListener.attach(processHandler);

        return processHandler;
    }

    public @NotNull GeneralCommandLine createCommandLine(boolean debug) {
        return OdinRunConfigurationUtils.createCommandLine(debug, getEnvironment(), options);
    }

    private void addMyConsoleFilters() {
        ConsoleFilterProvider[] filterProviders = ConsoleFilterProvider.FILTER_PROVIDERS.getExtensions();
        for (ConsoleFilterProvider provider : filterProviders) {
            for (Filter filter : provider.getDefaultFilters(getEnvironment().getProject())) {
                addConsoleFilters(filter);
            }
        }
    }

    private boolean isDebug() {
        return getEnvironment().getExecutor().getId().equals(DefaultDebugExecutor.EXECUTOR_ID);
    }


}
