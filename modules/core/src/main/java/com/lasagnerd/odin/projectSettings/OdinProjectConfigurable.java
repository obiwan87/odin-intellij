package com.lasagnerd.odin.projectSettings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.lasagnerd.odin.codeInsight.symbols.OdinSdkService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class OdinProjectConfigurable implements Configurable {

    private final Project project;
    private OdinProjectSettings sdkSettings;

    public OdinProjectConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Odin";
    }

    @Override
    public @Nullable JComponent createComponent() {

        OdinProjectSettings odinProjectSettings = new OdinProjectSettings();
        this.sdkSettings = odinProjectSettings;
        return odinProjectSettings.getComponent();
    }

    @Override
    public boolean isModified() {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        boolean sameSdkPath = sdkSettings.getSdkPath()
                .equals(state.sdkPath);

        boolean sameBuildFlags = sdkSettings.getBuildFlags()
                .equals(state.extraBuildFlags);

        boolean sameSemanticAnnotatorEnabled = sdkSettings.isSemanticAnnotatorEnabled()
                == settingsService.isSemanticAnnotatorEnabled();

        boolean debuggerIdModified = !sdkSettings.getDebuggerId().equals(state.getDebuggerId());
        boolean debuggerPathModified = !sdkSettings.getDebuggerPath().equals(state.getDebuggerPath());
        boolean odinCheckerEnabledModified = sdkSettings.isOdinCheckerEnabled() != settingsService.isOdinCheckerEnabled();
        boolean highlightUnknownReferencesEnabledModified = sdkSettings.isHighlightUnknownReferencesEnabled() != settingsService.isHighlightUnknownReferencesEnabled();

        return !sameSdkPath
                || !sameBuildFlags
                || !sameSemanticAnnotatorEnabled
                || debuggerIdModified
                || debuggerPathModified
                || odinCheckerEnabledModified
                || highlightUnknownReferencesEnabledModified;
    }

    @Override
    public void apply() {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        if(!Objects.equals(state.getSdkPath(), sdkSettings.getSdkPath())) {
            OdinSdkService.getInstance(project).invalidateCache();
        }

        apply(state, sdkSettings);

        OdinSdkLibraryManager.addOrUpdateOdinSdkLibrary(project, sdkSettings.getSdkPath());
    }

    public static void apply(OdinProjectSettingsState state, OdinProjectSettings sdkSettings) {
        state.setSdkPath(sdkSettings.getSdkPath());
        state.setExtraBuildFlags(sdkSettings.getBuildFlags());
        state.setSemanticAnnotatorEnabled(sdkSettings.isSemanticAnnotatorEnabled() ? "true" : "false");
        state.setDebuggerId(sdkSettings.getDebuggerId());
        state.setDebuggerPath(sdkSettings.getDebuggerPath());
        state.setOdinCheckerEnabled(sdkSettings.isOdinCheckerEnabled() ? "true" : "false");
        state.setHighlightUnknownReferencesEnabled(sdkSettings.isHighlightUnknownReferencesEnabled() ? "true" : "false");
    }

    @Override
    public void reset() {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        String sdkPath = state.getSdkPath();
        String extraBuildFlags = state.getExtraBuildFlags();
        sdkSettings.setSdkPath(sdkPath);
        sdkSettings.setBuildFlags(extraBuildFlags);
        sdkSettings.setSemanticAnnotatorEnabled(settingsService.isSemanticAnnotatorEnabled());
        sdkSettings.setDebuggerPath(state.getDebuggerPath());
        sdkSettings.setDebuggerId(state.getDebuggerId());
        sdkSettings.setOdinCheckerEnabled(settingsService.isOdinCheckerEnabled());
        sdkSettings.setHighlightUnknownReferences(settingsService.isHighlightUnknownReferencesEnabled());
    }

    @Override
    public void disposeUIResources() {
        this.sdkSettings = null;
    }

}
