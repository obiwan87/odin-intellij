package com.lasagnerd.odin.projectStructure.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.NlsContexts;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.projectSettings.OdinProjectSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdinModuleBuilder extends ModuleBuilder {
    public OdinModuleBuilder() {

    }

    @Override
    public ModuleType<?> getModuleType() {
        return OdinModuleType.INSTANCE;
    }

    @Override
    public String getGroupName() {
        return "Other";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return "Odin";
    }

    @Override
    public String getName() {
        return "Odin";
    }

    @Override
    public @NlsContexts.DetailedDescription String getDescription() {
        return "Creates a new Odin module";
    }

    @Override
    public @Nullable @NonNls String getBuilderId() {
        return "ODIN_MODULE_BUILDER";
    }

    @Override
    public Icon getNodeIcon() {
        return OdinIcons.OdinFileType;
    }

    @Override
    public boolean isOpenProjectSettingsAfter() {
        return false;
    }

    @Override
    public boolean isTemplateBased() {
        return false;
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{
                new OdinModuleWizardStep()
        };
    }

    @Override
    public @Nullable ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {

        return new OdinModuleWizardStep();
    }

    private static class OdinModuleWizardStep extends ModuleWizardStep {

        private OdinProjectSettings projectSettings;

        @Override
        public JComponent getComponent() {
            projectSettings = new OdinProjectSettings();
            return projectSettings.getComponent();
        }

        @Override
        public void updateDataModel() {
            System.out.println("Hello");
        }

        @Override
        public void disposeUIResources() {
            if (projectSettings != null) {
                projectSettings.dispose();
                projectSettings = null;
            }
            super.disposeUIResources();
        }
    }
}
