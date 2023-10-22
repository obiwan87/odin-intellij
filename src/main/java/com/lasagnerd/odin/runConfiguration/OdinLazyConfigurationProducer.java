package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.lang.OdinLanguage;
import org.jetbrains.annotations.NotNull;

public class OdinLazyConfigurationProducer extends LazyRunConfigurationProducer<OdinRunConfiguration> {


    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConfigurationTypeUtil.findConfigurationType(OdinRunConfigurationType.class).getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull OdinRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        // Check if current file is an Odin file
        PsiElement psiLocation = context.getPsiLocation();
        if (psiLocation == null) return false;

        PsiFile containingFile = psiLocation.getContainingFile();
        Language language = containingFile.getLanguage();
        if (language.isKindOf(OdinLanguage.INSTANCE)) {
            OdinRunConfigurationOptions options = configuration.getOptions();
            options.setProjectDirectoryPath(context.getProject().getBasePath());
            configuration.setName(containingFile.getName());

            return true;
        }


        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull OdinRunConfiguration configuration, @NotNull ConfigurationContext context) {
        PsiElement psiLocation = context.getPsiLocation();
        if (psiLocation == null) return false;

        Language language = psiLocation.getLanguage();
        if (language.equals(OdinLanguage.INSTANCE)) {
            return true;
        }
        return false;
    }
}
