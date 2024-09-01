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
                .equals(OdinSdkConfigPersistentState.getInstance(project).sdkPath);

        boolean sameBuildFlags = sdkSettingsComponent.getBuildFlags()
                .equals(OdinSdkConfigPersistentState.getInstance(project).extraBuildFlags);

        boolean sameSemanticAnnotatorEnabled = sdkSettingsComponent.isSemanticAnnotatorEnabled()
                == OdinSdkConfigPersistentState.getInstance(project).isSemanticAnnotatorEnabled();
        return !sameSdkPath || !sameBuildFlags || !sameSemanticAnnotatorEnabled;
    }

    @Override
    public void apply() {
        OdinSdkConfigPersistentState config = OdinSdkConfigPersistentState.getInstance(project);
        String sdkPath = sdkSettingsComponent.getSdkPath();
        config.setSdkPath(sdkPath);
        config.setExtraBuildFlags(sdkSettingsComponent.getBuildFlags());
        config.setSemanticAnnotatorEnabled(sdkSettingsComponent.isSemanticAnnotatorEnabled() ? "true" : "false");

        OdinSdkLibraryManager.addOrUpdateOdinSdkLibrary(project, sdkPath);
    }

    @Override
    public void reset() {
        String sdkPath = OdinSdkConfigPersistentState.getInstance(project).sdkPath;
        String extraBuildFlags = OdinSdkConfigPersistentState.getInstance(project).extraBuildFlags;
        boolean semanticAnnotatorEnabled = OdinSdkConfigPersistentState.getInstance(project).isSemanticAnnotatorEnabled();
        sdkSettingsComponent.setSdkPath(sdkPath);
        sdkSettingsComponent.setBuildFlags(extraBuildFlags);
        sdkSettingsComponent.setSemanticAnnotatorEnabled(semanticAnnotatorEnabled);
    }

    @Override
    public void disposeUIResources() {
        this.sdkSettingsComponent = null;
    }

}
