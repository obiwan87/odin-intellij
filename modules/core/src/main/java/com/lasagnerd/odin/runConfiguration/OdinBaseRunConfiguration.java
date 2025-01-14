package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectConfigurable;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public abstract class OdinBaseRunConfiguration<T extends OdinBaseRunConfigurationOptions> extends LocatableConfigurationBase<T> {
    protected OdinBaseRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, @Nullable String name) {
        super(project, factory, name);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        expandAndCheck(getOptions().getPackageDirectoryPath(), "Project directory");
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

    @Override
    public @NotNull T getOptions() {
        return (T) super.getOptions();
    }


    private String expandPath(String s) {
        ProgramParametersConfigurator configurator = new ProgramParametersConfigurator();
        return configurator.expandPathAndMacros(s, null, getProject());
    }

    protected void expandAndCheck(String path, String label) throws RuntimeConfigurationError {
        path = expandPath(path);
        checkSet(path, label);
        checkExists(path, label);
    }

    protected static void checkExists(String projectDirectoryPath, String label) throws RuntimeConfigurationError {
        File file = new File(projectDirectoryPath);
        if (!file.exists()) {
            throw new RuntimeConfigurationError(label + " does not exist");
        }
    }

    protected static void checkSet(String projectDirectoryPath, String label) throws RuntimeConfigurationError {
        if (projectDirectoryPath == null || projectDirectoryPath.isEmpty()) {
            throw new RuntimeConfigurationError(label + " is not set");
        }
    }
}
