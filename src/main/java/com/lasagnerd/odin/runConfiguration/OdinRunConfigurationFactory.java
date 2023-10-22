package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinRunConfigurationFactory extends ConfigurationFactory {
    public OdinRunConfigurationFactory(OdinRunConfigurationType odinRunConfigurationType) {
        super(odinRunConfigurationType);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new OdinRunConfiguration(project, this, "Odin");
    }

    @Override
    public @Nullable Class<? extends BaseState> getOptionsClass() {
        return OdinRunConfigurationOptions.class;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return OdinRunConfigurationType.ID;
    }
}
