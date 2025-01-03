package com.lasagnerd.odin.runConfiguration.build;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunProfileWithCompileBeforeLaunchOption;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.runConfiguration.OdinBaseRunConfiguration;
import com.lasagnerd.odin.runConfiguration.OdinBaseRunConfigurationOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinBuildRunConfiguration extends OdinBaseRunConfiguration<OdinBaseRunConfigurationOptions>
        implements RunProfileWithCompileBeforeLaunchOption{
    protected OdinBuildRunConfiguration(@NotNull Project project,
                                        @NotNull ConfigurationFactory factory,
                                        @Nullable String name) {
        super(project, factory, name);
    }

    private String expandPath(String s) {
        ProgramParametersConfigurator configurator = new ProgramParametersConfigurator();
        return configurator.expandPathAndMacros(s, null, getProject());
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new OdinBuildRunConfigurationSettingsEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new OdinBuildRunCommandLineState(environment, getOptions());
    }

    @Override
    public @NotNull OdinBuildRunConfigurationOptions getOptions() {
        return (OdinBuildRunConfigurationOptions) super.getOptions();
    }
}
