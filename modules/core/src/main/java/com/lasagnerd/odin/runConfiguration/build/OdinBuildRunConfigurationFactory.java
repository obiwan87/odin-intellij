package com.lasagnerd.odin.runConfiguration.build;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinBuildRunConfigurationFactory extends ConfigurationFactory {
    public OdinBuildRunConfigurationFactory(OdinBuildRunConfigurationType odinBuildRunConfigurationType) {
        super(odinBuildRunConfigurationType);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new OdinBuildRunConfiguration(project, this, "Odin");
    }

    @Override
    public @Nullable Class<? extends BaseState> getOptionsClass() {
        return OdinBuildRunConfigurationOptions.class;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return OdinBuildRunConfigurationType.ID;
    }

    @Override
    public void configureDefaultSettings(@NotNull RunnerAndConfigurationSettings settings) {
        super.configureDefaultSettings(settings);
    }
}
