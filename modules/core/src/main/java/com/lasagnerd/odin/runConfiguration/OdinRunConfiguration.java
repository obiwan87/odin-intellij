package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.projectSettings.OdinProjectConfigurable;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsState;
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

    private String expandPath(String s) {
        ProgramParametersConfigurator configurator = new ProgramParametersConfigurator();
        return configurator.expandPathAndMacros(s, null, getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        expandAndCheck(getOptions().getProjectDirectoryPath(), "Project directory");
        expandAndCheck(getOptions().getWorkingDirectory(), "Working directory");
        checkSet(getOptions().getOutputPath(), "Output path");

        OdinProjectSettingsState state = OdinProjectSettingsService.getInstance(getProject()).getState();
        String sdkPath = state.sdkPath;
        if (sdkPath == null || sdkPath.isEmpty()) {
            throw new RuntimeConfigurationError("Odin SDK path is not set",
                    () -> ShowSettingsUtil.getInstance().showSettingsDialog(getProject(),
                            OdinProjectConfigurable.class, null));
        }

        File sdkFile = new File(sdkPath);
        if (!sdkFile.exists()) {
            throw new RuntimeConfigurationError("Odin SDK path does not exist");
        }
    }

    private void expandAndCheck(String path, String label) throws RuntimeConfigurationError {
        path = expandPath(path);
        checkSet(path, label);
        checkExists(path, label);
    }

    private static void checkExists(String projectDirectoryPath, String label) throws RuntimeConfigurationError {
        File file = new File(projectDirectoryPath);
        if (!file.exists()) {
            throw new RuntimeConfigurationError(label + " does not exist");
        }
    }

    private static void checkSet(String projectDirectoryPath, String label) throws RuntimeConfigurationError {
        if (projectDirectoryPath == null || projectDirectoryPath.isEmpty()) {
            throw new RuntimeConfigurationError(label + " is not set");
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


}
