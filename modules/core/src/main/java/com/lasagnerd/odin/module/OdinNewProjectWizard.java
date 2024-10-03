package com.lasagnerd.odin.module;

import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard;
import com.lasagnerd.odin.OdinIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinNewProjectWizard implements LanguageGeneratorNewProjectWizard {
    @Override
    public @NotNull Icon getIcon() {
        return OdinIcons.OdinFileType;
    }

    @Override
    public @NotNull String getName() {
        return "Odin";
    }

    @Override
    public @NotNull NewProjectWizardStep createStep(@NotNull NewProjectWizardStep newProjectWizardStep) {
        return new OdinNewProjectStep(newProjectWizardStep);
    }
}
