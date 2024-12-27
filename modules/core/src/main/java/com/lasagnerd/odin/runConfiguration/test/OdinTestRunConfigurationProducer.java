package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.lang.psi.OdinConstantInitDeclaration;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

public class OdinTestRunConfigurationProducer extends LazyRunConfigurationProducer<OdinTestRunConfiguration> {
    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConfigurationTypeUtil.findConfigurationType(OdinTestRunConfigurationType.class).getConfigurationFactories()[0];
    }


    @Override
    protected boolean setupConfigurationFromContext(@NotNull OdinTestRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        // Check if current file is an Odin file

        PsiElement psiElement = sourceElement.get();
        OdinConstantInitDeclaration testProcedure = OdinRunConfigurationUtils.getTestProcedure(psiElement);
        if (testProcedure != null) {
            @Nullable String name = testProcedure.getDeclaredIdentifierList().getFirst().getName();

            if (name != null) {
                String packageClauseName = OdinInsightUtils.getPackageClauseName(testProcedure);
                if (packageClauseName != null) {
                    String qualifiedName = packageClauseName + "." + name;
                    configuration.setName("Test " + qualifiedName);
                    configuration.getOptions().setTestKind("Package");
                    configuration.getOptions().setTestNames(qualifiedName);
                    populatePaths(configuration, context, testProcedure);
                }
            }
            return true;
        }

        if (OdinRunConfigurationUtils.isPackageClause(psiElement)) {
            OdinFile containingFile = (OdinFile) psiElement.getContainingFile();
            OdinFileScope fileScope = containingFile.getFileScope();
            boolean hasHanyTestProcedures = fileScope != null && fileScope.getFullSymbolTable()
                    .getSymbols().stream()
                    .anyMatch(s -> s.getSymbolType() == OdinSymbolType.PROCEDURE
                            && s.getDeclaration() instanceof OdinConstantInitDeclaration constantInitDeclaration
                            && OdinInsightUtils.containsAttribute(constantInitDeclaration.getAttributesDefinitionList(), "test")
                    );

            if (hasHanyTestProcedures) {
                populatePaths(configuration, context, psiElement);
                VirtualFile containingVirtualFile = OdinImportUtils.getContainingVirtualFile(psiElement);
                configuration.setName("Test " + containingVirtualFile.getNameWithoutExtension());
                configuration.getOptions().setTestKind("File");
                configuration.getOptions().setTestFilePath(containingVirtualFile.getPath());
                return true;
            }
        }

        return false;
    }

    private static void populatePaths(@NotNull OdinTestRunConfiguration configuration, @NotNull ConfigurationContext context, PsiElement psiElement) {
        VirtualFile containingVirtualFile = OdinImportUtils.getContainingVirtualFile(psiElement);
        VirtualFile parent = containingVirtualFile.getParent();
        if (parent != null) {
            configuration.getOptions().setPackageDirectoryPath(parent.getPath());
        }
        configuration.getOptions().setWorkingDirectory(context.getProject().getBasePath());
    }


    @Override
    public boolean isConfigurationFromContext(@NotNull OdinTestRunConfiguration configuration, @NotNull ConfigurationContext context) {
        PsiElement psiLocation = context.getPsiLocation();
        if (psiLocation == null) return false;

        OdinTestRunConfigurationOptions options = configuration.getOptions();
        String projectDirectoryPath = options.getPackageDirectoryPath();
        PsiFile containingFile = psiLocation.getContainingFile();

        if (!(containingFile instanceof OdinFile odinFile)) return false;

        boolean matchingTestKind = false;
        if (Objects.equals(options.getTestKind(), "Package")) {
            OdinConstantInitDeclaration testProcedure = OdinRunConfigurationUtils.getTestProcedure(psiLocation);

            if (testProcedure != null) {
                String packageClauseName = OdinInsightUtils.getPackageClauseName(testProcedure);
                String myTestName = testProcedure.getName() + "." + packageClauseName;
                String testNames = StringUtils.removeEnd(configuration.getOptions().getTestNames(), ",");
                matchingTestKind = Objects.equals(myTestName, testNames);
            }
        }

        if (Objects.equals(options.getTestKind(), "File")) {
            boolean packageClause = OdinRunConfigurationUtils.isPackageClause(psiLocation);
            if (packageClause) {
                VirtualFile containingVirtualFile = OdinImportUtils.getContainingVirtualFile(psiLocation);
                Path myPath = Path.of(containingVirtualFile.getPath());
                String testFilePath = configuration.getOptions().getTestFilePath();
                if (testFilePath != null) {
                    try {
                        Path theirPath = Path.of(testFilePath);
                        matchingTestKind = theirPath.equals(myPath);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        PsiDirectory containingDirectory = odinFile.getContainingDirectory();

        if (containingDirectory != null) {
            String myPath = containingDirectory.getVirtualFile().getPath();
            return matchingTestKind && projectDirectoryPath.equals(myPath);
        }

        return false;
    }
}
