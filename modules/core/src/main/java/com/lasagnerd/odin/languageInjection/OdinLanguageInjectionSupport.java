package com.lasagnerd.odin.languageInjection;

import com.intellij.psi.PsiLanguageInjectionHost;
import com.lasagnerd.odin.lang.psi.OdinStringLiteral;
import org.intellij.plugins.intelliLang.inject.AbstractLanguageInjectionSupport;
import org.intellij.plugins.intelliLang.inject.config.BaseInjection;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class OdinLanguageInjectionSupport extends AbstractLanguageInjectionSupport {

    public static final String ID = "Odin";

    @Override
    public @NotNull String getId() {
        return ID;
    }

    @Override
    public Class<?> @NotNull [] getPatternClasses() {
        return new Class[0];
    }

    @Override
    public boolean isApplicableTo(PsiLanguageInjectionHost host) {
        return host instanceof OdinStringLiteral;
    }

    @Override
    public boolean useDefaultInjector(PsiLanguageInjectionHost host) {
        return false;
    }

    @Override
    public BaseInjection createInjection(Element element) {
        return super.createInjection(element);
    }
}
