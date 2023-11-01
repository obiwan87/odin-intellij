package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OdinRunConfiguration extends RunConfigurationBase<OdinRunConfigurationOptions> {
    protected OdinRunConfiguration(@NotNull Project project, @Nullable ConfigurationFactory factory, @Nullable String name) {
        super(project, factory, name);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        String projectDirectoryPath = getOptions().getProjectDirectoryPath();

        if (projectDirectoryPath == null || projectDirectoryPath.isEmpty()) {
            throw new RuntimeConfigurationError("Project directory path is not set");
        }

        File file = new File(projectDirectoryPath);
        if (!file.exists()) {
            throw new RuntimeConfigurationError("Project directory does not exist");
        }

        String sdkPath = OdinSdkConfigPersistentState.getInstance(getProject()).getSdkPath();

        if (sdkPath == null || sdkPath.isEmpty()) {
            throw new RuntimeConfigurationError("Odin SDK path is not set",
                    () -> ShowSettingsUtil.getInstance().showSettingsDialog(getProject(),
                            OdinSdkConfigurable.class, null));
        }

        File sdkFile = new File(sdkPath);
        if (!sdkFile.exists()) {
            throw new RuntimeConfigurationError("Odin SDK path does not exist");
        }
    }

    @Override
    @NotNull
    protected OdinRunConfigurationOptions getOptions() {
        return (OdinRunConfigurationOptions) super.getOptions();
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new OdinRunConfigurationSettingsEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new CommandLineState(environment) {
            @Override
            protected @NotNull ProcessHandler startProcess() throws ExecutionException {
                OdinRunConfigurationOptions options = getOptions();

                List<String> command = new ArrayList<>();
                String sdkPath = OdinSdkConfigPersistentState.getInstance(getProject()).getSdkPath();
                String projectDirectoryPath = options.getProjectDirectoryPath();
                String compilerPath = sdkPath + "/odin";
                String compilerOptions = Objects.requireNonNullElse(options.getCompilerOptions(), "");
                String outputPathString = Objects.requireNonNullElse(options.getOutputPath(), "");

                addCommandPart(command, compilerPath);
                addCommandPart(command, "run");
                addCommandPart(command, projectDirectoryPath);
                addCommandPart(command, compilerOptions);

                String basePath = getProject().getBasePath();
                if (!outputPathString.isEmpty()) {
                    ProgramParametersConfigurator configurator = new ProgramParametersConfigurator();
                    String expandedPath = configurator.expandPathAndMacros(outputPathString, null, getProject());

                    Path outputPath = getAbsolutePath(getProject(), expandedPath);
                    if (!outputPath.getParent().toFile().exists()) {
                        boolean success = outputPath.getParent().toFile().mkdirs();
                        if (!success) {
                            throw new ExecutionException("Failed to create output directory");
                        }
                    }

                    addCommandPart(command, "-out:" + expandedPath);
                }

                if(options.getProgramArguments() != null && !options.getProgramArguments().isEmpty()) {
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

                OSProcessHandler processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);
                ProcessTerminatedListener.attach(processHandler);
                return processHandler;
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
        };
    }


}
