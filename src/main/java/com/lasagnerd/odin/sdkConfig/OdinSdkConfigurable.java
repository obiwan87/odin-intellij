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
        return !sdkSettingsComponent.getSdkPath().equals(OdinSdkConfigPersistentState.getInstance(project).getSdkPath());
    }

    @Override
    public void apply() {
        String sdkPath = sdkSettingsComponent.getSdkPath();
        OdinSdkConfigPersistentState.getInstance(project).setSdkPath(sdkPath);
    }

    @Override
    public void reset() {
        String sdkPath = OdinSdkConfigPersistentState.getInstance(project).getSdkPath();
        sdkSettingsComponent.setSdkPath(sdkPath);
    }

    @Override
    public void disposeUIResources() {
        this.sdkSettingsComponent = null;
    }



}
