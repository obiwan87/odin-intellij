package com.lasagnerd.odin.projectStructure.project;

import com.intellij.platform.GeneratorPeerImpl;
import com.lasagnerd.odin.projectSettings.OdinProjectSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinProjectGeneratorPeer extends GeneratorPeerImpl<OdinProjectSettings> {
    OdinProjectSettings projectSettings = new OdinProjectSettings();

    @Override
    public @NotNull OdinProjectSettings getSettings() {
        return projectSettings;
    }


    @Override
    public @NotNull JComponent getComponent() {
        return projectSettings.getComponent();
    }
}
