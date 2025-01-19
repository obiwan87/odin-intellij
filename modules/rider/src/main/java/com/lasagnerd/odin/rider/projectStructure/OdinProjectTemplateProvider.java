package com.lasagnerd.odin.rider.projectStructure;

import com.jetbrains.rd.util.lifetime.Lifetime;
import com.jetbrains.rd.util.reactive.IProperty;
import com.jetbrains.rider.projectView.projectTemplates.NewProjectDialogContext;
import com.jetbrains.rider.projectView.projectTemplates.providers.ProjectTemplateProvider;
import com.jetbrains.rider.projectView.projectTemplates.templateTypes.ProjectTemplateType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class OdinProjectTemplateProvider implements ProjectTemplateProvider {
    @Override
    public @NotNull IProperty<Boolean> isReady() {
        return null;
    }

    @Override
    public @NotNull IProperty<Set<ProjectTemplateType>> load(@NotNull Lifetime lifetime, @NotNull NewProjectDialogContext newProjectDialogContext) {
        return null;
    }
}
