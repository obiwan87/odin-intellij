package com.lasagnerd.odin.projectStructure.module.rootTypes.source;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.roots.ui.configuration.ModuleSourceRootEditHandler;
import com.intellij.ui.DarculaColors;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class OdinSourceRootEditHandler extends ModuleSourceRootEditHandler<OdinSourceRootProperties> {
    private static final Color SOURCES_COLOR = new JBColor(new Color(0x0A50A1), DarculaColors.BLUE);

    protected OdinSourceRootEditHandler() {
        super(OdinSourceRootType.INSTANCE);
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getRootTypeName() {
        return "Odin Sources";
    }

    @Override
    public @NotNull Icon getRootIcon() {
        return AllIcons.Modules.SourceRoot;
    }

    @Override
    public @Nullable Icon getFolderUnderRootIcon() {
        return null;
    }

    @Override
    public @Nullable CustomShortcutSet getMarkRootShortcutSet() {
        return null;
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getRootsGroupTitle() {
        return "Source Folders";
    }

    @Override
    public @NotNull Color getRootsGroupColor() {
        return SOURCES_COLOR;
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getUnmarkRootButtonText() {
        return "Unmark Source";
    }


}
