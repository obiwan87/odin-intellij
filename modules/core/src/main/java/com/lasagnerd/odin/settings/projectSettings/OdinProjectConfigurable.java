package com.lasagnerd.odin.settings.projectSettings;

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

        OdinToolchainState toolchain = OdinProjectToolchainService.getInstance(project).getToolchain();
        boolean sdkPathModified = toolchain == null || !projectSettings.getSdkPath().equals(toolchain.libraryPath);
        boolean compilerPathModified = toolchain == null || !projectSettings.getCompilerPath().equals(toolchain.compilerPath);
        boolean toolchainModified = !projectSettings.getToolchainId().equals(state.toolchainId);
        boolean toolchainNameModified = toolchain == null || !projectSettings.getToolchainName().equals(toolchain.name);

        boolean buildFlagsModified = !projectSettings.getBuildFlags()
                .equals(state.extraBuildFlags);

        boolean semanticAnnotatorEnabledModified = !projectSettings.isSemanticAnnotatorEnabled()
                == settingsService.isSemanticAnnotatorEnabled();

        boolean debuggerIdModified = toolchain == null || !projectSettings.getDebuggerId().equals(toolchain.debuggerId);
        boolean debuggerPathModified = toolchain == null || !projectSettings.getDebuggerPath().equals(toolchain.debuggerPath);
        boolean odinCheckerEnabledModified = projectSettings.isOdinCheckerEnabled() != settingsService.isOdinCheckerEnabled();
        boolean highlightUnknownReferencesEnabledModified = projectSettings.isHighlightUnknownReferencesEnabled() != settingsService.isHighlightUnknownReferencesEnabled();
        boolean conditionalSymbolResolutionEnabledModified = projectSettings.isConditionalSymbolResolutionCheckboxEnabled() != settingsService.isConditionalSymbolResolutionEnabled();
        boolean cachedEnabledModified = projectSettings.isCacheEnabled() != settingsService.isCacheEnabled();


        return sdkPathModified
                || buildFlagsModified
                || compilerPathModified
                || toolchainModified
                || toolchainNameModified
                || semanticAnnotatorEnabledModified
                || debuggerIdModified
                || debuggerPathModified
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
        if (!Objects.equals(previousCompilerPath, projectSettings.getCompilerPath())
                || !Objects.equals(previousLibraryPath, projectSettings.getSdkPath())) {
            OdinSdkService.getInstance(project).refreshCache();
        }

        if (StringUtil.isNotEmpty(projectSettings.getCompilerPath())) {
            File odinBinaryFile = new File(projectSettings.getCompilerPath());
            if (!odinBinaryFile.exists() || !odinBinaryFile.isFile()) {
                throw new ConfigurationException("Odin executable path is not valid");
            }
        }

        if (StringUtil.isNotEmpty(projectSettings.getSdkPath()) && !new File(projectSettings.getSdkPath()).isDirectory())
            throw new ConfigurationException("Odin library path is not valid");

        OdinToolchainState toolchain = applyToolchain(project, state, projectSettings);
        state.toolchainId = toolchain.id;
        apply(state, projectSettings);

        OdinSdkLibraryManager.addOrUpdateOdinSdkLibrary(project,
                previousLibraryPath,
                projectSettings.getSdkPath()
        );
    }

    public static OdinToolchainState applyToolchain(Project project, OdinProjectSettingsState state, OdinProjectSettings projectSettings) {
        OdinToolchainService registry = OdinToolchainService.getInstance();
        OdinToolchainState toolchain = registry.find(projectSettings.getToolchainId());
        if (toolchain == null) {
            toolchain = new OdinToolchainState();
            registry.add(toolchain);
        }
        toolchain.name = projectSettings.getToolchainName().isBlank()
                ? "Odin Toolchain (" + project.getName() + ")" : projectSettings.getToolchainName();
        toolchain.compilerPath = projectSettings.getCompilerPath();
        toolchain.libraryPath = projectSettings.getSdkPath();
        toolchain.debuggerId = projectSettings.getDebuggerId();
        toolchain.debuggerPath = projectSettings.getDebuggerPath();
        state.toolchainId = toolchain.id;
        return toolchain;
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

        OdinToolchainState toolchain = OdinProjectToolchainService.getInstance(project).getToolchain();
        String sdkPath = toolchain == null ? "" : Objects.requireNonNullElse(toolchain.libraryPath, "");
        String extraBuildFlags = state.getExtraBuildFlags();
        projectSettings.setToolchainId(state.toolchainId);
        projectSettings.setToolchainName(toolchain == null ? "" : Objects.requireNonNullElse(toolchain.name, ""));
        projectSettings.setCompilerPath(toolchain == null ? "" : Objects.requireNonNullElse(toolchain.compilerPath, ""));
        projectSettings.setSdkPath(sdkPath);
        projectSettings.setBuildFlags(extraBuildFlags);
        projectSettings.setSemanticAnnotatorEnabled(settingsService.isSemanticAnnotatorEnabled());
        projectSettings.setDebuggerPath(toolchain == null ? "" : Objects.requireNonNullElse(toolchain.debuggerPath, ""));
        projectSettings.setDebuggerId(toolchain == null ? "" : Objects.requireNonNullElse(toolchain.debuggerId, ""));
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
