package com.lasagnerd.odin.runConfiguration;

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
        if (containingFile == null) return false;

        Language language = containingFile.getLanguage();
        if (language.isKindOf(OdinLanguage.INSTANCE)) {
            Project project = context.getProject();
            PsiDirectory containingDirectory = psiLocation.getContainingFile().getContainingDirectory();

            if(containingDirectory == null) return false;

            OdinRunConfigurationOptions options = configuration.getOptions();
            String packagePath = containingDirectory.getVirtualFile().getPath();
            options.setProjectDirectoryPath(packagePath);
            options.setWorkingDirectory(project.getBasePath());

            boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
            String packageName = containingDirectory.getName();

            String outputPath = project.getBasePath() + "/bin" + (isWindows ? "/%s.exe" : "/%s").formatted(packageName);
            options.setOutputPath(outputPath);

            configuration.setName(containingFile.getName());
            return true;
        }


        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull OdinRunConfiguration configuration, @NotNull ConfigurationContext context) {
        PsiElement psiLocation = context.getPsiLocation();
        if (psiLocation == null) return false;

        OdinRunConfigurationOptions options = configuration.getOptions();
        String projectDirectoryPath = options.getProjectDirectoryPath();
        String myPath = psiLocation.getContainingFile().getContainingDirectory().getVirtualFile().getPath();

        return projectDirectoryPath.equals(myPath);
    }
}
