package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunProfileWithCompileBeforeLaunchOption;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.runConfiguration.OdinBaseRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinTestRunConfiguration extends OdinBaseRunConfiguration<OdinTestRunConfigurationOptions>
        implements RunProfileWithCompileBeforeLaunchOption {
    protected OdinTestRunConfiguration(@NotNull Project project,
                                       @NotNull ConfigurationFactory factory,
                                       @Nullable String name) {
        super(project, factory, name);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new OdinTestRunConfigurationSettingsEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new OdinTestRunCommandLineState(environment, getOptions());
    }

}
