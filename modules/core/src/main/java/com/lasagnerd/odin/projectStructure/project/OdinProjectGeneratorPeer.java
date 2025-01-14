package com.lasagnerd.odin.projectStructure.project;

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
        projectSettings.getComponent();
        projectSettings.setOdinCheckerEnabled(true);
        projectSettings.setSemanticAnnotatorEnabled(true);
    }

    @Override
    public @NotNull OdinProjectSettings getSettings() {
        return projectSettings;
    }


    @Override
    public @NotNull JComponent getComponent() {
        return projectSettings.getComponent();
    }
}
