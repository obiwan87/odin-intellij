package com.lasagnerd.odin.module;

import com.intellij.ide.wizard.AbstractNewProjectWizardStep;
import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.openapi.project.Project;
import com.intellij.ui.dsl.builder.AlignX;
import com.intellij.ui.dsl.builder.Panel;
import com.lasagnerd.odin.projectSettings.OdinProjectSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinNewProjectStep extends AbstractNewProjectWizardStep {
    private final OdinProjectSettings projectSettings = new OdinProjectSettings();

    public OdinNewProjectStep(@NotNull NewProjectWizardStep parentStep) {
        super(parentStep);
    }

    @Override
    public void setupUI(@NotNull Panel builder) {
        JComponent component = projectSettings.getComponent();
        builder.row((JLabel) null, r -> {
            r.cell(component).align(AlignX.FILL);
            return null;
        });
    }

    @Override
    public void setupProject(@NotNull Project project) {
        super.setupProject(project);
        System.out.println("setupProject() called");
    }
}
