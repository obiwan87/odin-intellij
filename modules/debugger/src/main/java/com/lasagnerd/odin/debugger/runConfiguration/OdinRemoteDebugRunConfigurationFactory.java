package com.lasagnerd.odin.debugger.runConfiguration;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinRemoteDebugRunConfigurationFactory extends ConfigurationFactory {
    public OdinRemoteDebugRunConfigurationFactory(@NotNull OdinRemoteDebugRunConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new OdinRemoteDebugRunConfiguration(project, this, "Odin remote debug");
    }

    @Override
    public @Nullable Class<? extends BaseState> getOptionsClass() {
        return OdinRemoteDebugRunConfigurationOptions.class;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return OdinRemoteDebugRunConfigurationType.ID;
    }

    @Override
    public void configureDefaultSettings(@NotNull RunnerAndConfigurationSettings settings) {

        super.configureDefaultSettings(settings);
    }
}
