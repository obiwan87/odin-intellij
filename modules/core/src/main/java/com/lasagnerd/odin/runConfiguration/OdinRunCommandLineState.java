package com.lasagnerd.odin.runConfiguration;

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
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.projectSettings.OdinSdkUtils;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Setter
@Getter
public class OdinRunCommandLineState extends CommandLineState {
    private final OdinRunConfigurationOptions options;

    public OdinRunCommandLineState(@NotNull ExecutionEnvironment environment,
                                   @NotNull OdinRunConfigurationOptions options) {
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
        return createCommandLine(debug, getEnvironment(), options);
    }

    public static @NotNull GeneralCommandLine createCommandLine(boolean debug,
                                                                ExecutionEnvironment environment,
                                                                OdinRunConfigurationOptions options) {
        Project project = environment.getProject();
        String mode = debug ? "build" : "run";
        String projectDirectoryPath = options.getProjectDirectoryPath();
        String programArguments = options.getProgramArguments();
        String compilerOptions = Objects.requireNonNullElse(options.getCompilerOptions(), "");
        String outputPathString = Objects.requireNonNullElse(options.getOutputPath(), "");
        String basePath = project.getBasePath();
        String workingDirectory;
        if (options.getWorkingDirectory() != null) {
            workingDirectory = options.getWorkingDirectory();
        } else {
            workingDirectory = basePath;
        }

        return OdinSdkUtils.createCommandLine(project, debug,
                mode,
                compilerOptions,
                outputPathString,
                projectDirectoryPath,
                programArguments,
                workingDirectory);
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
