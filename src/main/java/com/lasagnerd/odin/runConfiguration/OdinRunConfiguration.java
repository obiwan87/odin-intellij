package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
        return new OdinRunConfigurationSettingsEditor();
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new CommandLineState(environment) {
            @Override
            protected @NotNull ProcessHandler startProcess() throws ExecutionException {

                List<String> command = new ArrayList<>();
                String sdkPath = OdinSdkConfigPersistentState.getInstance(getProject()).getSdkPath();
                String projectDirectoryPath = getOptions().getProjectDirectoryPath();

                String compilerPath = sdkPath + "/odin";

                String compilerOptions = Objects.requireNonNullElse(getOptions().getCompilerOptions(), "");

                addCommandPart(command, compilerPath);
                addCommandPart(command, "run");
                addCommandPart(command, projectDirectoryPath);
                addCommandPart(command, compilerOptions);

                GeneralCommandLine commandLine = new GeneralCommandLine(command);
                commandLine.setWorkDirectory(getProject().getBasePath());
                OSProcessHandler processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);
                ProcessTerminatedListener.attach(processHandler);
                return processHandler;
            }

            public void addCommandPart(List<String> list, String item) {
                if (item != null && !item.isEmpty()) {
                    list.add(item);
                }
            }
        };
    }
}
