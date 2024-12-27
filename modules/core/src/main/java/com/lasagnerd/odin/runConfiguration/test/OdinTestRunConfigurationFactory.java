package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinTestRunConfigurationFactory extends ConfigurationFactory {
    public OdinTestRunConfigurationFactory(OdinTestRunConfigurationType odinTestRunConfigurationType) {
        super(odinTestRunConfigurationType);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new OdinTestRunConfiguration(project, this, "Odin");
    }

    @Override
    public @Nullable Class<? extends BaseState> getOptionsClass() {
        return OdinTestRunConfigurationOptions.class;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return OdinTestRunConfigurationType.ID;
    }

    @Override
    public void configureDefaultSettings(@NotNull RunnerAndConfigurationSettings settings) {
        super.configureDefaultSettings(settings);
    }
}
