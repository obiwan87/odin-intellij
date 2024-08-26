package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.lasagnerd.odin.sdkConfig.OdinSdkUtils;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class OdinRunCommandLineState extends CommandLineState {
    private final OdinRunConfigurationOptions options;
    private boolean debug;

    public OdinRunCommandLineState(@NotNull ExecutionEnvironment environment,
                                   @NotNull OdinRunConfigurationOptions options) {
        super(environment);
        this.options = options;
    }

    @Override
    protected @NotNull ProcessHandler startProcess() throws ExecutionException {

        ConsoleFilterProvider[] filterProviders = ConsoleFilterProvider.FILTER_PROVIDERS.getExtensions();
        for (ConsoleFilterProvider provider : filterProviders) {
            for (Filter filter : provider.getDefaultFilters(getEnvironment().getProject())) {
                addConsoleFilters(filter);
            }
        }

        GeneralCommandLine commandLine = createCommandLine();

        OSProcessHandler processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);
        ProcessTerminatedListener.attach(processHandler);
        return processHandler;
    }

    public @NotNull GeneralCommandLine createCommandLine() throws ExecutionException {
        OdinRunConfigurationOptions options = getOptions();

        List<String> command = new ArrayList<>();
        String projectDirectoryPath = options.getProjectDirectoryPath();
        String odinBinaryPath = OdinSdkUtils.getOdinBinaryPath(getEnvironment().getProject());
        if(odinBinaryPath == null) {
            throw new ExecutionException("'odin' executable not found. Please setup SDK.");
        }
        String compilerPath = FileUtil.toSystemIndependentName(odinBinaryPath);
        String compilerOptions = Objects.requireNonNullElse(options.getCompilerOptions(), "");
        String outputPathString = Objects.requireNonNullElse(options.getOutputPath(), "");

        addCommandPart(command, compilerPath);
        addCommandPart(command, "run");
        addCommandPart(command, projectDirectoryPath);
        if (isDebug()) {
            addCommandPart(command, "-debug");
        }
        addCommandPart(command, compilerOptions);


        Project project = getEnvironment().getProject();

        String basePath = project.getBasePath();
        if (!outputPathString.isEmpty()) {
            ProgramParametersConfigurator configurator = new ProgramParametersConfigurator();
            String expandedPath = configurator.expandPathAndMacros(outputPathString, null, project);

            Path outputPath = getAbsolutePath(project, expandedPath);
            if (!outputPath.getParent().toFile().exists()) {
                boolean success = outputPath.getParent().toFile().mkdirs();
                if (!success) {
                    throw new ExecutionException("Failed to create output directory");
                }
            }

            addCommandPart(command, "-out:" + expandedPath);
        }

        if (options.getProgramArguments() != null && !options.getProgramArguments().isEmpty()) {
            addCommandPart(command, "--");
            addCommandPart(command, options.getProgramArguments());
        }

        GeneralCommandLine commandLine = new GeneralCommandLine(command);
        String workingDirectory;
        if (options.getWorkingDirectory() != null) {
            workingDirectory = options.getWorkingDirectory();
        } else {
            workingDirectory = basePath;
        }
        commandLine.setWorkDirectory(workingDirectory);
        return commandLine;
    }

    public Path getAbsolutePath(Project project, String expandedPath) {
        String projectBasePathString = project.getBasePath();
        if (projectBasePathString == null) {
            throw new RuntimeException("Project base path is null");
        }
        Path basePath = Paths.get(projectBasePathString);
        Path outputPathFile = Paths.get(expandedPath);

        return basePath.resolve(outputPathFile);
    }

    public void addCommandPart(List<String> list, String item) {
        if (item != null && !item.isEmpty()) {
            list.add(item);
        }
    }


}
