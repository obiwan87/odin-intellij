package com.lasagnerd.odin.formatting;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.lasagnerd.odin.lang.OdinLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinLanguageCodeStyleProvider extends LanguageCodeStyleSettingsProvider  {

    @org.intellij.lang.annotations.Language("Odin")
    @Override
    public @Nullable String getCodeSample(@NotNull SettingsType settingsType) {

        return """
                package main
                                
                import "core:fmt"
                
                main :: proc() {
                    program := "+ + * 😃 - /"
                    accumulator:=0
                    accumulator += 1
                        
                    for token in program {
                        switch token {
                        case '+': accumulator += 1
                        case '-': accumulator -= 1
                        case '*': accumulator *= 2
                        case '/': accumulator /= 2
                        case '😃': accumulator *= accumulator
                        case:
                        }
                    }
                    fmt.printf("The program \\"%s\\" calculates the value %d\\n", program, accumulator)
                }
                """;
    }

    @Override
    public @NotNull Language getLanguage() {
        return OdinLanguage.INSTANCE;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        super.customizeSettings(consumer, settingsType);
        consumer.showAllStandardOptions();
    }
}
