package com.lasagnerd.odin.sdkConfig;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;
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
        OdinDebuggerToolchain[] extensions = OdinDebuggerToolchain.DEBUGGER_TOOLCHAIN.getExtensions();
        OdinSdkSettingsComponent odinSdkSettingsComponent = new OdinSdkSettingsComponent(extensions, project);
        this.sdkSettingsComponent = odinSdkSettingsComponent;
        return odinSdkSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        OdinSdkConfigPersistentState state = OdinSdkConfigPersistentState.getInstance(project);
        boolean sameSdkPath = sdkSettingsComponent.getSdkPath()
                .equals(state.sdkPath);

        boolean sameBuildFlags = sdkSettingsComponent.getBuildFlags()
                .equals(state.extraBuildFlags);

        boolean sameSemanticAnnotatorEnabled = sdkSettingsComponent.isSemanticAnnotatorEnabled()
                == state.isSemanticAnnotatorEnabled();

        boolean debuggerIdModified = !sdkSettingsComponent.getDebuggerId().equals(state.getDebuggerId());
        boolean debuggerPathModified = !sdkSettingsComponent.getDebuggerPath().equals(state.getDebuggerPath());
        return !sameSdkPath
                || !sameBuildFlags
                || !sameSemanticAnnotatorEnabled
                || debuggerIdModified
                || debuggerPathModified;
    }

    @Override
    public void apply() {
        OdinSdkConfigPersistentState config = OdinSdkConfigPersistentState.getInstance(project);
        String sdkPath = sdkSettingsComponent.getSdkPath();
        config.setSdkPath(sdkPath);
        config.setExtraBuildFlags(sdkSettingsComponent.getBuildFlags());
        config.setSemanticAnnotatorEnabled(sdkSettingsComponent.isSemanticAnnotatorEnabled() ? "true" : "false");
        config.setDebuggerId(sdkSettingsComponent.getDebuggerId());
        config.setDebuggerPath(sdkSettingsComponent.getDebuggerPath());

        OdinSdkLibraryManager.addOrUpdateOdinSdkLibrary(project, sdkPath);
    }

    @Override
    public void reset() {
        OdinSdkConfigPersistentState state = OdinSdkConfigPersistentState.getInstance(project);
        String sdkPath = state.sdkPath;
        String extraBuildFlags = state.extraBuildFlags;
        boolean semanticAnnotatorEnabled = state.isSemanticAnnotatorEnabled();
        sdkSettingsComponent.setSdkPath(sdkPath);
        sdkSettingsComponent.setBuildFlags(extraBuildFlags);
        sdkSettingsComponent.setSemanticAnnotatorEnabled(semanticAnnotatorEnabled);
        sdkSettingsComponent.setDebuggerPath(state.getDebuggerPath());
        sdkSettingsComponent.setDebuggerId(state.getDebuggerId());
    }

    @Override
    public void disposeUIResources() {
        this.sdkSettingsComponent = null;
    }

}
