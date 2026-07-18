package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class OdinProjectConfigurable implements Configurable {

    private final Project project;
    private OdinProjectSettings projectSettings;

    public OdinProjectConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Compiler & Debugger";
    }

    @Override
    public @Nullable JComponent createComponent() {
        // Ensure a legacy project is represented in the application registry before the selector is populated.
        OdinProjectToolchainService.getInstance(project).getToolchain();
        OdinProjectSettings odinProjectSettings = new OdinProjectSettings();
        this.projectSettings = odinProjectSettings;
        return odinProjectSettings.getComponent();
    }

    @Override
    public boolean isModified() {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();
        boolean toolchainModified = !projectSettings.getToolchainId().equals(state.toolchainId);

        boolean buildFlagsModified = !projectSettings.getBuildFlags()
                .equals(state.extraBuildFlags);

        boolean semanticAnnotatorEnabledModified = !projectSettings.isSemanticAnnotatorEnabled()
                == settingsService.isSemanticAnnotatorEnabled();

        boolean odinCheckerEnabledModified = projectSettings.isOdinCheckerEnabled() != settingsService.isOdinCheckerEnabled();
        boolean highlightUnknownReferencesEnabledModified = projectSettings.isHighlightUnknownReferencesEnabled() != settingsService.isHighlightUnknownReferencesEnabled();
        boolean conditionalSymbolResolutionEnabledModified = projectSettings.isConditionalSymbolResolutionCheckboxEnabled() != settingsService.isConditionalSymbolResolutionEnabled();
        boolean cachedEnabledModified = projectSettings.isCacheEnabled() != settingsService.isCacheEnabled();


        return buildFlagsModified
                || toolchainModified
                || semanticAnnotatorEnabledModified
                || odinCheckerEnabledModified
                || highlightUnknownReferencesEnabledModified
                || conditionalSymbolResolutionEnabledModified
                || cachedEnabledModified
                ;
    }

    @Override
    public void apply() throws ConfigurationException {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        OdinProjectToolchainService projectToolchains = OdinProjectToolchainService.getInstance(project);
        String previousLibraryPath = projectToolchains.getLibraryPath().orElse("");
        String previousCompilerPath = projectToolchains.getCompilerPath().orElse("");
        OdinToolchainState selected = OdinToolchainService.getInstance().find(projectSettings.getToolchainId());
        String selectedCompilerPath = selected == null ? "" : Objects.requireNonNullElse(selected.compilerPath, "");
        String selectedLibraryPath = selected == null ? "" : Objects.requireNonNullElse(selected.libraryPath, "");
        if (!Objects.equals(previousCompilerPath, selectedCompilerPath)
                || !Objects.equals(previousLibraryPath, selectedLibraryPath)) {
            OdinSdkService.getInstance(project).refreshCache();
        }
        state.toolchainId = projectSettings.getToolchainId();
        apply(state, projectSettings);

        OdinSdkLibraryManager.addOrUpdateOdinSdkLibrary(project,
                previousLibraryPath,
                selectedLibraryPath
        );
    }

    public static void apply(OdinProjectSettingsState state, OdinProjectSettings sdkSettings) {
        state.setExtraBuildFlags(sdkSettings.getBuildFlags());
        state.setSemanticAnnotatorEnabled(sdkSettings.isSemanticAnnotatorEnabled() ? "true" : "false");
        state.setOdinCheckerEnabled(sdkSettings.isOdinCheckerEnabled() ? "true" : "false");
        state.setHighlightUnknownReferencesEnabled(sdkSettings.isHighlightUnknownReferencesEnabled() ? "true" : "false");
        state.setConditionalSymbolResolutionEnabled(sdkSettings.isConditionalSymbolResolutionCheckboxEnabled() ? "true" : "false");
        state.setCacheEnabled(sdkSettings.isCacheEnabled() ? "true" : "false");
    }

    @Override
    public void reset() {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        OdinProjectToolchainService.getInstance(project).getToolchain();
        String extraBuildFlags = state.getExtraBuildFlags();
        projectSettings.setToolchainId(state.toolchainId);
        projectSettings.setBuildFlags(extraBuildFlags);
        projectSettings.setSemanticAnnotatorEnabled(settingsService.isSemanticAnnotatorEnabled());
        projectSettings.setOdinCheckerEnabled(settingsService.isOdinCheckerEnabled());
        projectSettings.setHighlightUnknownReferences(settingsService.isHighlightUnknownReferencesEnabled());
        projectSettings.setConditionalSymbolResolutionEnabledCheckboxEnabled(settingsService.isConditionalSymbolResolutionEnabled());
        projectSettings.setCacheEnabled(settingsService.isCacheEnabled());
    }

    @Override
    public void disposeUIResources() {
        this.projectSettings.dispose();
        this.projectSettings = null;
    }

}
