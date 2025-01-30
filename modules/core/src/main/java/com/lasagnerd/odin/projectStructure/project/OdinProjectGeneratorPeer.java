package com.lasagnerd.odin.projectStructure.project;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.platform.GeneratorPeerImpl;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinProjectGeneratorPeer extends GeneratorPeerImpl<OdinProjectSettings> {
    OdinProjectSettings projectSettings = new OdinProjectSettings();

    public OdinProjectGeneratorPeer() {
        setDefaultValues();
    }

    private void setDefaultValues() {
        projectSettings.initializeWithDefaultValues();
    }

    @Override
    public @NotNull OdinProjectSettings getSettings() {
        return projectSettings;
    }

    @Override
    public void buildUI(@NotNull SettingsStep settingsStep) {
        super.buildUI(settingsStep);
    }

    @Override
    public @NotNull JComponent getComponent(@NotNull TextFieldWithBrowseButton myLocationField, @NotNull Runnable checkValid) {
        return projectSettings.getComponent();
    }
}
