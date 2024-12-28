package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.lang.psi.OdinConstantInitDeclaration;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class OdinTestRunConfigurationProducer extends LazyRunConfigurationProducer<OdinTestRunConfiguration> {
    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConfigurationTypeUtil.findConfigurationType(OdinTestRunConfigurationType.class).getConfigurationFactories()[0];
    }


    private static void createFileBasedTest(@NotNull OdinTestRunConfiguration configuration, @NotNull ConfigurationContext context, PsiElement psiElement) {
        populatePaths(configuration, context, psiElement);
        VirtualFile containingVirtualFile = OdinInsightUtils.getContainingVirtualFile(psiElement);
        configuration.setName("Test " + containingVirtualFile.getNameWithoutExtension());
        configuration.getOptions().setTestKind("File");
        configuration.getOptions().setTestFilePath(containingVirtualFile.getPath());
    }

    private static void populatePaths(@NotNull OdinTestRunConfiguration configuration, @NotNull ConfigurationContext context, PsiElement psiElement) {
        VirtualFile containingVirtualFile = OdinInsightUtils.getContainingVirtualFile(psiElement);
        VirtualFile parent = containingVirtualFile.getParent();
        if (parent != null) {
            configuration.getOptions().setPackageDirectoryPath(parent.getPath());
        }
        configuration.getOptions().setWorkingDirectory(context.getProject().getBasePath());
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull OdinTestRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        // Check if current file is an Odin file

        PsiElement psiElement = sourceElement.get();

        if (psiElement instanceof PsiDirectory psiDirectory) {
            OdinFile firstTestFile = OdinRunConfigurationUtils.getFirstOdinFile(psiDirectory,
                    configuration.getProject(),
                    odinFile -> odinFile.getVirtualFile().getNameWithoutExtension().endsWith("_test")
                            || !OdinRunConfigurationUtils.findTestProcedures(odinFile).isEmpty()
            );

            if (firstTestFile != null) {
                String packageClauseName = OdinInsightUtils.getPackageClauseName(firstTestFile);
                if (packageClauseName != null) {
                    configuration.setName("Test " + packageClauseName);
                    populatePaths(configuration, context, firstTestFile);
                    configuration.getOptions().setTestKind("Package");
                    return true;
                }
            }
            return false;
        }

        if (psiElement instanceof OdinFile file) {
            List<OdinConstantInitDeclaration> testProcedures = OdinRunConfigurationUtils.findTestProcedures(file);
            if (!testProcedures.isEmpty()) {
                createFileBasedTest(configuration, context, file);
                return true;
            }
            return false;
        }

        OdinConstantInitDeclaration testProcedure = OdinRunConfigurationUtils.getTestProcedure(psiElement);
        if (testProcedure != null) {
            @Nullable String name = testProcedure.getDeclaredIdentifierList().getFirst().getName();

            if (name != null) {
                String packageClauseName = OdinInsightUtils.getPackageClauseName(testProcedure);
                if (packageClauseName != null) {
                    String qualifiedName = packageClauseName + "." + name;
                    configuration.setName("Test " + qualifiedName);
                    populatePaths(configuration, context, testProcedure);
                    configuration.getOptions().setTestKind("Package");
                    configuration.getOptions().setTestNames(qualifiedName);
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
                createFileBasedTest(configuration, context, psiElement);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull OdinTestRunConfiguration configuration, @NotNull ConfigurationContext context) {
        PsiElement psiLocation = context.getPsiLocation();
        if (psiLocation == null) return false;

        OdinTestRunConfigurationOptions options = configuration.getOptions();
        String projectDirectoryPath = options.getPackageDirectoryPath();

        boolean matchingTestKind = false;
        if (psiLocation instanceof PsiDirectory directory) {
            if (Objects.equals(options.getTestKind(), "Package")) {
                String packageDirectoryPath = options.getPackageDirectoryPath();
                ProgramParametersConfigurator configurator = new ProgramParametersConfigurator();
                String expandedPath = configurator.expandPathAndMacros(packageDirectoryPath, null, configuration.getProject());

                VirtualFile canonicalFile = directory.getVirtualFile().getCanonicalFile();
                if (canonicalFile != null) {
                    return Path.of(expandedPath).equals(canonicalFile.toNioPath());
                }
            }
        } else {
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
                if (packageClause || psiLocation instanceof OdinFile) {
                    VirtualFile containingVirtualFile = OdinInsightUtils.getContainingVirtualFile(psiLocation);
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
            PsiFile containingFile = psiLocation.getContainingFile();
            if (!(containingFile instanceof OdinFile odinFile)) return false;
            PsiDirectory containingDirectory = odinFile.getContainingDirectory();
            if (containingDirectory != null) {
                String myPath = containingDirectory.getVirtualFile().getPath();
                return matchingTestKind && projectDirectoryPath.equals(myPath);
            }
        }

        return false;
    }
}
