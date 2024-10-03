package com.lasagnerd.odin.projectStructure.module;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.ModuleType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

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

    @Override
    public boolean isSupportedRootType(JpsModuleSourceRootType<?> type) {
        return type instanceof OdinSourceRootType || type instanceof OdinCollectionRootType;
    }
}
