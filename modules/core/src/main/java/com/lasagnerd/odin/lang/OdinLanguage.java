package com.lasagnerd.odin.lang;

import com.intellij.lang.Language;

public class OdinLanguage extends Language {
    public static final Language INSTANCE = new OdinLanguage();

    protected OdinLanguage() {
        super("Odin");
    }
}
