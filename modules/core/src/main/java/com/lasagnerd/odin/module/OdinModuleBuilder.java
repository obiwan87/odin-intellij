package com.lasagnerd.odin.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.NlsContexts;
import com.lasagnerd.odin.OdinIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
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
        return true;
    }

    @Override
    public boolean isTemplateBased() {
        return true;
    }
}
