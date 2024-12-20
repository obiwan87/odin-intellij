package com.lasagnerd.odin.projectSettings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Objects;

public class OdinProjectConfigurable implements Configurable {

    private final Project project;
    private OdinProjectSettings projectSettings;

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
        this.projectSettings = odinProjectSettings;
        return odinProjectSettings.getComponent();
    }

    @Override
    public boolean isModified() {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        boolean sameSdkPath = projectSettings.getSdkPath()
                .equals(state.sdkPath);

        boolean sameBuildFlags = projectSettings.getBuildFlags()
                .equals(state.extraBuildFlags);

        boolean sameSemanticAnnotatorEnabled = projectSettings.isSemanticAnnotatorEnabled()
                == settingsService.isSemanticAnnotatorEnabled();

        boolean debuggerIdModified = !projectSettings.getDebuggerId().equals(state.getDebuggerId());
        boolean debuggerPathModified = !projectSettings.getDebuggerPath().equals(state.getDebuggerPath());
        boolean odinCheckerEnabledModified = projectSettings.isOdinCheckerEnabled() != settingsService.isOdinCheckerEnabled();
        boolean highlightUnknownReferencesEnabledModified = projectSettings.isHighlightUnknownReferencesEnabled() != settingsService.isHighlightUnknownReferencesEnabled();
        boolean conditionalSymbolResolutionEnabledModified = projectSettings.isConditionalSymbolResolutionCheckboxEnabled() != settingsService.isConditionalSymbolResolutionEnabled();


        return !sameSdkPath
                || !sameBuildFlags
                || !sameSemanticAnnotatorEnabled
                || debuggerIdModified
                || debuggerPathModified
                || odinCheckerEnabledModified
                || highlightUnknownReferencesEnabledModified
                || conditionalSymbolResolutionEnabledModified
                ;
    }

    @Override
    public void apply() throws ConfigurationException {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        if (!Objects.equals(state.getSdkPath(), projectSettings.getSdkPath())) {
            OdinSdkService.getInstance(project).refreshCache();
        }

        if (StringUtil.isNotEmpty(projectSettings.getSdkPath())) {
            String odinBinaryPath = OdinSdkUtils.getOdinBinaryPath(projectSettings.getSdkPath());
            File odinBinaryFile = new File(odinBinaryPath);
            if (!odinBinaryFile.exists() || !odinBinaryFile.isFile()) {
                throw new ConfigurationException("SDK path is not valid");
            }
        }

        apply(state, projectSettings);

        OdinSdkLibraryManager.addOrUpdateOdinSdkLibrary(project, projectSettings.getSdkPath());
    }

    public static void apply(OdinProjectSettingsState state, OdinProjectSettings sdkSettings) {
        state.setSdkPath(sdkSettings.getSdkPath());
        state.setExtraBuildFlags(sdkSettings.getBuildFlags());
        state.setSemanticAnnotatorEnabled(sdkSettings.isSemanticAnnotatorEnabled() ? "true" : "false");
        state.setDebuggerId(sdkSettings.getDebuggerId());
        state.setDebuggerPath(sdkSettings.getDebuggerPath());
        state.setOdinCheckerEnabled(sdkSettings.isOdinCheckerEnabled() ? "true" : "false");
        state.setHighlightUnknownReferencesEnabled(sdkSettings.isHighlightUnknownReferencesEnabled() ? "true" : "false");
        state.setConditionalSymbolResolutionEnabled(sdkSettings.isConditionalSymbolResolutionCheckboxEnabled() ? "true" : "false");
    }

    @Override
    public void reset() {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        String sdkPath = state.getSdkPath();
        String extraBuildFlags = state.getExtraBuildFlags();
        projectSettings.setSdkPath(sdkPath);
        projectSettings.setBuildFlags(extraBuildFlags);
        projectSettings.setSemanticAnnotatorEnabled(settingsService.isSemanticAnnotatorEnabled());
        projectSettings.setDebuggerPath(state.getDebuggerPath());
        projectSettings.setDebuggerId(state.getDebuggerId());
        projectSettings.setOdinCheckerEnabled(settingsService.isOdinCheckerEnabled());
        projectSettings.setHighlightUnknownReferences(settingsService.isHighlightUnknownReferencesEnabled());
        projectSettings.setConditionalSymbolResolutionEnabledCheckbox(settingsService.isConditionalSymbolResolutionEnabled());
    }

    @Override
    public void disposeUIResources() {
        this.projectSettings.dispose();
        this.projectSettings = null;
    }

}
