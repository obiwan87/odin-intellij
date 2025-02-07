package com.lasagnerd.odin.languageInjection;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.injection.general.Injection;
import com.intellij.lang.injection.general.LanguageInjectionPerformer;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.lang.psi.OdinStringLiteral;
import org.jetbrains.annotations.NotNull;

public class OdinLanguageInjectionPerformer implements LanguageInjectionPerformer {

    @Override
    public boolean isPrimary() {
        return true;
    }

    @Override
    public boolean performInjection(@NotNull MultiHostRegistrar registrar,
                                    @NotNull Injection injection,
                                    @NotNull PsiElement context) {
        if (!(context instanceof OdinStringLiteral stringLiteral)) {
            return false;
        }

        Language injectedLanguage = injection.getInjectedLanguage();
        if (injectedLanguage == null) {
            return false;
        }

        registrar.startInjecting(injectedLanguage);
        registrar.addPlace(null, null, stringLiteral, TextRange.create(1, stringLiteral.getTextLength() - 1));
        registrar.doneInjecting();

        return true;
    }
}
