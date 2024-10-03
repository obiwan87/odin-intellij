package com.lasagnerd.odin.externalBuildSystem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.externalSystem.autoimport.*;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.service.project.autoimport.ProjectAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.lasagnerd.odin.ols.OlsConfiguration;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class OdinExternalSystemProjectAware implements ExternalSystemProjectAware{

    public static final ProjectSystemId ODIN_SYSTEM_ID = new ProjectSystemId(
            "odin", "Odin"
    );
    Project project;

    public OdinExternalSystemProjectAware(Project project) {
        this.project = project;
    }


    @Override
    public @NotNull ExternalSystemProjectId getProjectId() {
        return new ExternalSystemProjectId(ODIN_SYSTEM_ID,
                Objects.requireNonNull(project.getBasePath()));
    }

    @Override
    public @NotNull Set<String> getSettingsFiles() {
        String basePath = project.getBasePath();
        if(basePath != null) {
            Path path = Path.of(basePath, "ols.json");
            return Set.of(path.toString());
        }
        return Collections.emptySet();
    }

    @Override
    public void subscribe(@NotNull ExternalSystemProjectListener externalSystemProjectListener, @NotNull Disposable disposable) {
        System.out.println("subscribe called");
    }

    @Override
    public void reloadProject(@NotNull ExternalSystemProjectReloadContext externalSystemProjectReloadContext) {
        for (String s : externalSystemProjectReloadContext.getSettingsFilesContext().getUpdated()) {
            System.out.println(s + " changed");
        }
        System.out.println("reload project called");
    }
}
