package com.lasagnerd.odin.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdinSdkConfigurable implements Configurable {
    private OdinSdkSettingsComponent sdkSettingsComponent;

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
        return !sdkSettingsComponent.getSdkPath().equals(OdinSdkConfigPersistentState.getInstance().getSdkPath());
    }

    @Override
    public void apply() {
        String sdkPath = sdkSettingsComponent.getSdkPath();
        OdinSdkConfigPersistentState.getInstance().setSdkPath(sdkPath);
    }

    @Override
    public void reset() {
        String sdkPath = OdinSdkConfigPersistentState.getInstance().getSdkPath();
        sdkSettingsComponent.setSdkPath(sdkPath);
    }

    @Override
    public void disposeUIResources() {
        this.sdkSettingsComponent = null;
    }



}
