package com.lasagnerd.odin.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.lasagnerd.odin.OdinIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinFileType extends LanguageFileType {
    public static final OdinFileType INSTANCE = new OdinFileType();
    protected OdinFileType() {
        super(OdinLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Odin";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Odin is a general-purpose programming language with distinct typing built for high performance, modern systems and data-oriented programming.";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "odin";
    }

    @Override
    public Icon getIcon() {
        return OdinIcons.OdinFileType;
    }
}
