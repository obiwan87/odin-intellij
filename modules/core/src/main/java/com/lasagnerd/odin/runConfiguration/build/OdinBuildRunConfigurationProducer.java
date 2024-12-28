package com.lasagnerd.odin.runConfiguration.build;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.lang.OdinLanguage;
import com.lasagnerd.odin.lang.psi.OdinConstantInitDeclaration;
import com.lasagnerd.odin.runConfiguration.OdinBaseRunConfigurationOptions;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import org.jetbrains.annotations.NotNull;

public class OdinBuildRunConfigurationProducer extends LazyRunConfigurationProducer<OdinBuildRunConfiguration> {


    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConfigurationTypeUtil.findConfigurationType(OdinBuildRunConfigurationType.class).getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull OdinBuildRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {

        PsiElement psiLocation = context.getPsiLocation();
        if (psiLocation == null) return false;
        OdinConstantInitDeclaration testProcedure = OdinRunConfigurationUtils.getTestProcedure(sourceElement.get());
        if (testProcedure != null) {
            return false;
        }

        if (OdinRunConfigurationUtils.isPackageClause(context.getPsiLocation())) {
            return false;
        }
        // Check if current file is an Odin file

        PsiFile containingFile = psiLocation.getContainingFile();
        if (containingFile == null) return false;

        Language language = containingFile.getLanguage();
        if (language.isKindOf(OdinLanguage.INSTANCE)) {
            Project project = context.getProject();
            PsiDirectory containingDirectory = psiLocation.getContainingFile().getContainingDirectory();

            if (containingDirectory == null) return false;

            OdinBaseRunConfigurationOptions options = configuration.getOptions();
            String packagePath = containingDirectory.getVirtualFile().getPath();
            options.setPackageDirectoryPath(packagePath);
            options.setWorkingDirectory(project.getBasePath());

            String outputPath = OdinBuildRunConfigurationOptions.OUTPUT_PATH_DEFAULT;
            options.setOutputPath(outputPath);

            configuration.setName(containingFile.getName());
            return true;
        }


        return false;
    }


    @Override
    public boolean isConfigurationFromContext(@NotNull OdinBuildRunConfiguration configuration, @NotNull ConfigurationContext context) {
        PsiElement psiLocation = context.getPsiLocation();
        if (psiLocation == null) return false;

        OdinBaseRunConfigurationOptions options = configuration.getOptions();
        String projectDirectoryPath = options.getPackageDirectoryPath();
        PsiFile containingFile = psiLocation.getContainingFile();

        if (containingFile == null) return false;

        PsiDirectory containingDirectory = containingFile.getContainingDirectory();

        String myPath = containingDirectory.getVirtualFile().getPath();

        return projectDirectoryPath.equals(myPath);
    }
}
