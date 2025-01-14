package com.lasagnerd.odin.projectStructure.project;

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.platform.DirectoryProjectGenerator;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinProjectSettingsStep extends ProjectSettingsStepBase<OdinProjectSettings> {
    public OdinProjectSettingsStep(DirectoryProjectGenerator<OdinProjectSettings> projectGenerator, AbstractNewProjectStep.AbstractCallback<OdinProjectSettings> callback) {
        super(projectGenerator, callback);
    }

    @Override
    protected JPanel createBasePanel() {
        JPanel basePanel = super.createBasePanel();
        OdinProjectSettings projectSettings = new OdinProjectSettings();
        JComponent component = projectSettings.getComponent();
        basePanel.add(component);

        return basePanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        super.actionPerformed(e);
    }
}
