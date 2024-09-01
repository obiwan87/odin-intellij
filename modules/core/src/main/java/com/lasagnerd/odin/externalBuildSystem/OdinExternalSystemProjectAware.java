package com.lasagnerd.odin.externalBuildSystem;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectAware;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectId;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectListener;
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectReloadContext;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class OdinExternalSystemProjectAware implements ExternalSystemProjectAware {
    Project project;

    public OdinExternalSystemProjectAware(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull ExternalSystemProjectId getProjectId() {
        return new ExternalSystemProjectId(new ProjectSystemId(
                "odin", "Odin"
        ), Objects.requireNonNull(project.getBasePath()));
    }

    @Override
    public @NotNull Set<String> getSettingsFiles() {
        return Set.of("ols.json");
    }

    @Override
    public void subscribe(@NotNull ExternalSystemProjectListener externalSystemProjectListener, @NotNull Disposable disposable) {
        System.out.println("subscribe called");
    }

    @Override
    public void reloadProject(@NotNull ExternalSystemProjectReloadContext externalSystemProjectReloadContext) {
        System.out.println("reload project called");
    }
}
