package com.lasagnerd.odin.formatting;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class OdinCodeStyleSettings extends CustomCodeStyleSettings {

    protected OdinCodeStyleSettings(@NotNull CodeStyleSettings container) {
        super("OdinCodeStyleSettings", container);
    }
}

