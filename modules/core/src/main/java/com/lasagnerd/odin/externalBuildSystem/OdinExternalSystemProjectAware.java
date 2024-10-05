package com.lasagnerd.odin.externalBuildSystem;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.externalSystem.autoimport.*;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class OdinExternalSystemProjectAware implements ExternalSystemProjectAware {

    public static final ProjectSystemId ODIN_SYSTEM_ID = new ProjectSystemId(
            "ols", "OLS"
    );
    Project project;
    private ExternalSystemProjectListener listener;
    private final ExternalSystemProjectId externalSystemProjectId;

    public OdinExternalSystemProjectAware(Project project) {
        this.project = project;
        externalSystemProjectId = new ExternalSystemProjectId(ODIN_SYSTEM_ID, Objects.requireNonNull(project.getBasePath()));
    }


    @Override
    public @NotNull ExternalSystemProjectId getProjectId() {
        return externalSystemProjectId;
    }

    @Override
    public @NotNull Set<String> getSettingsFiles() {
        String basePath = project.getBasePath();
        if (basePath != null) {
            Path path = Path.of(basePath, "ols.json");
            return Set.of(FileUtil.toSystemIndependentName(path.toString()));
        }
        return Collections.emptySet();
    }

    @Override
    public void subscribe(@NotNull ExternalSystemProjectListener externalSystemProjectListener, @NotNull Disposable disposable) {
        this.listener = externalSystemProjectListener;
        System.out.println("subscribe called");
        externalSystemProjectListener.onSettingsFilesListChange();
    }

    @Override
    public void reloadProject(@NotNull ExternalSystemProjectReloadContext externalSystemProjectReloadContext) {
        for (String s : externalSystemProjectReloadContext.getSettingsFilesContext().getUpdated()) {
            System.out.println(s + " changed");
        }

        for (String s : externalSystemProjectReloadContext.getSettingsFilesContext().getCreated()) {
            System.out.println(s + " created");
        }

        for (String s : externalSystemProjectReloadContext.getSettingsFilesContext().getDeleted()) {
            System.out.println(s + " deleted");
        }

        if (listener != null) {
            System.out.println("Reloading");
        }
    }
}
