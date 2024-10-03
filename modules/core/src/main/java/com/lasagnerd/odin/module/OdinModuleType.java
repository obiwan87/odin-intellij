package com.lasagnerd.odin.module;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinModuleType extends ModuleType<OdinModuleBuilder> {
    public static final OdinModuleType INSTANCE = new OdinModuleType();
    public static final String ODIN_MODULE = "ODIN_MODULE";

    protected OdinModuleType() {
        super(ODIN_MODULE);
    }

    @Override
    public @NotNull OdinModuleBuilder createModuleBuilder() {
        return new OdinModuleBuilder();
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getName() {
        return "Odin Module";
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getDescription() {
        return "Odin module";
    }

    @Override
    public @NotNull Icon getNodeIcon(boolean isOpened) {
        return AllIcons.Nodes.Module;
    }


}
