package com.lasagnerd.odin.debugger.runConfiguration;

import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EmptyRunProfileState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinRemoteDebugRunConfiguration extends RunConfigurationBase<OdinRemoteDebugRunConfigurationOptions> {
    protected OdinRemoteDebugRunConfiguration(@NotNull Project project, @Nullable ConfigurationFactory factory, @Nullable String name) {
        super(project, factory, name);
    }

    @Override
    public @NotNull OdinRemoteDebugRunConfigurationSettingsEditor getConfigurationEditor() {
        return new OdinRemoteDebugRunConfigurationSettingsEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return EmptyRunProfileState.INSTANCE;
    }

    @Override
    public boolean canRunOn(@NotNull ExecutionTarget target) {
        return true;
    }

    @Override
    public @NotNull OdinRemoteDebugRunConfigurationOptions getOptions() {
        return (OdinRemoteDebugRunConfigurationOptions) super.getOptions();
    }


}
