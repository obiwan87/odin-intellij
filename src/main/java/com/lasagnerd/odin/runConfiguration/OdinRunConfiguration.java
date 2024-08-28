package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class OdinRunConfiguration extends LocatableConfigurationBase<OdinRunConfigurationOptions>
        implements RunProfileWithCompileBeforeLaunchOption{
    protected OdinRunConfiguration(@NotNull Project project,
                                   @NotNull ConfigurationFactory factory,
                                   @Nullable String name) {
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
    public OdinRunConfigurationOptions getOptions() {
        return (OdinRunConfigurationOptions) super.getOptions();
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new OdinRunConfigurationSettingsEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new OdinRunCommandLineState(environment, getOptions());
    }

    public @NotNull String getOutputPath() {
        return getOptions().getOutputPath();
    }

//    @Override
//    public @NotNull List<BeforeRunTask<?>> getBeforeRunTasks() {
//        return List.of(new OdinBuildBeforeRunTask(OdinBuildBeforeRunTaskProvider.ID));
//    }
}
