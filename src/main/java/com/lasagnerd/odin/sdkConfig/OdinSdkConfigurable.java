package com.lasagnerd.odin.sdkConfig;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdinSdkConfigurable implements Configurable {

    private final Project project;
    private OdinSdkSettingsComponent sdkSettingsComponent;

    public OdinSdkConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Odin";
    }

    @Override
    public @Nullable JComponent createComponent() {
        OdinSdkSettingsComponent odinSdkSettingsComponent = new OdinSdkSettingsComponent();
        this.sdkSettingsComponent = odinSdkSettingsComponent;
        return odinSdkSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        boolean sameSdkPath = sdkSettingsComponent.getSdkPath()
                .equals(OdinSdkConfigPersistentState.getInstance(project).getSdkPath());
        boolean sameDirectoryToCompile = sdkSettingsComponent.getDirectoryToCompile()
                .equals(OdinSdkConfigPersistentState.getInstance(project).getDirectoryToCompile());
        boolean sameBuildFlags = sdkSettingsComponent.getBuildFlags()
                .equals(OdinSdkConfigPersistentState.getInstance(project).getDirectoryToCompile());
        return !sameSdkPath || !sameDirectoryToCompile || !sameBuildFlags;
    }

    @Override
    public void apply() {
        OdinSdkConfigPersistentState config = OdinSdkConfigPersistentState.getInstance(project);
        String sdkPath = sdkSettingsComponent.getSdkPath();
        String directoryToCompile = sdkSettingsComponent.getDirectoryToCompile();
        config.setSdkPath(sdkPath);
        config.setDirectoryToCompile(directoryToCompile);
        config.setExtraBuildFlags(sdkSettingsComponent.getBuildFlags());
    }

    @Override
    public void reset() {
        String sdkPath = OdinSdkConfigPersistentState.getInstance(project).getSdkPath();
        String directoryToCompile = OdinSdkConfigPersistentState.getInstance(project).getDirectoryToCompile();
        String extraBuildFlags = OdinSdkConfigPersistentState.getInstance(project).getExtraBuildFlags();
        sdkSettingsComponent.setSdkPath(sdkPath);
        sdkSettingsComponent.setDirectoryToCompile(directoryToCompile);
        sdkSettingsComponent.setBuildFlags(extraBuildFlags);
    }

    @Override
    public void disposeUIResources() {
        this.sdkSettingsComponent = null;
    }

}
