package com.lasagnerd.odin.sdkConfig;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

@Setter
@Getter
@State(
        name = "com.lasagnerd.odin.settings.OdinSdkConfigPersistentState",
        storages = @Storage("OdinSdkConfig.xml")
)
public class OdinSdkConfigPersistentState implements PersistentStateComponent<OdinSdkConfigPersistentState> {

    public String sdkPath = "";
    public String extraBuildFlags = "";

    public static OdinSdkConfigPersistentState getInstance(Project project) {
        return project.getService(OdinSdkConfigPersistentState.class);
    }

    @Nullable
    public static String getOdinBinaryPath(Project project) {
        if (project == null) {
            return null;
        }
        OdinSdkConfigPersistentState sdkConfig = getInstance(project);
        if (sdkConfig.getSdkPath() == null)
            return null;

        String systemIndependentPath = FileUtil.toSystemIndependentName(sdkConfig.getSdkPath());
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        return StringUtils.removeEnd(systemIndependentPath, "/") + "/" + "odin" + (isWindows ? ".exe" : "");
    }

    @Override
    public @Nullable OdinSdkConfigPersistentState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull OdinSdkConfigPersistentState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static Optional<String> getSdkPath(Project project) {
        OdinSdkConfigPersistentState sdkConfig = OdinSdkConfigPersistentState.getInstance(project);
        if (sdkConfig == null) {
            return Optional.empty();
        }

        String sdkPath = sdkConfig.getSdkPath();
        if (sdkPath == null || sdkPath.isBlank()) {
            return Optional.empty();
        }

        try {
            Path ignore = Path.of(sdkPath);
        } catch (InvalidPathException e) {
            return Optional.empty();
        }

        return Optional.of(sdkPath);
    }



}
