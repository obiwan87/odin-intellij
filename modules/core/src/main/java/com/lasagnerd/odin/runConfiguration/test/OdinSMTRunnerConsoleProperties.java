package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PathUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.OdinConstantInitDeclaration;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

public class OdinSMTRunnerConsoleProperties extends SMTRunnerConsoleProperties implements SMCustomMessagesParsing {

    public static final String ODIN_TEST_RUNNER = "Odin Test Runner";

    public OdinSMTRunnerConsoleProperties(@NotNull RunConfiguration config,
                                          @NotNull String testFrameworkName,
                                          @NotNull Executor executor) {
        super(config, testFrameworkName, executor);
    }

    public OdinSMTRunnerConsoleProperties(@NotNull Project project,
                                          @NotNull RunProfile config,
                                          @NotNull String testFrameworkName,
                                          @NotNull Executor executor) {
        super(project, config, testFrameworkName, executor);
    }

    private static @NotNull OdinTestProceduresLocations getTestProcedureLocations(List<OdinFile> filesInPackage) {
        Map<String, Path> procedureToFilePath = new HashMap<>();
        Map<Path, List<String>> fileToProcedureName = new HashMap<>();
        for (OdinFile file : filesInPackage) {
            VirtualFile containingVirtualFile = OdinInsightUtils.getContainingVirtualFile(file);
            Path virtualFilePath = Path.of(containingVirtualFile.getPath());
            List<OdinConstantInitDeclaration> testProcedures = OdinRunConfigurationUtils.findTestProcedures(file);
            for (OdinConstantInitDeclaration testProcedure : testProcedures) {
                String qualifiedCanonicalName = OdinInsightUtils.getQualifiedCanonicalName(testProcedure);
                procedureToFilePath.put(qualifiedCanonicalName, virtualFilePath);
                fileToProcedureName
                        .computeIfAbsent(virtualFilePath, v -> new ArrayList<>())
                        .add(qualifiedCanonicalName);
            }
        }
        return new OdinTestProceduresLocations(procedureToFilePath, fileToProcedureName);
    }

    @Override
    public @Nullable SMTestLocator getTestLocator() {
        return new OdinTestLocator();
    }

    @Override
    public OutputToGeneralTestEventsConverter createTestEventsConverter(@NotNull String testFrameworkName, @NotNull TestConsoleProperties consoleProperties) {
        OdinTestRunConfiguration configuration = getConfiguration();
        OdinTestRunConfigurationOptions options = configuration.getOptions();
        boolean isPackageKind = Objects.equals(options.getTestKind(), "Package");
        boolean isFileKind = Objects.equals(options.getTestKind(), "File");
        if (isPackageKind || isFileKind) {
            // TODO(lasagnerd) Pass map with qualified procedure name to file path

            OdinTestProceduresLocations testProcedureLocations = OdinTestProceduresLocations.EMPTY;
            if (isFileKind && options.getTestFilePath() != null && PathUtil.isValidFileName(options.getTestFilePath())) {
                VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(options.getTestFilePath()));
                if (virtualFile != null) {
                    PsiFile file = PsiManager.getInstance(getProject()).findFile(virtualFile);
                    if (file instanceof OdinFile odinFile) {
                        testProcedureLocations = getTestProcedureLocations(List.of(odinFile));
                    }
                }
            }

            if (isPackageKind && options.getPackageDirectoryPath() != null && PathUtil.isValidFileName(options.getPackageDirectoryPath())) {
                List<OdinFile> filesInPackage = OdinImportUtils.getFilesInPackage(getProject(),
                        Path.of(options.getPackageDirectoryPath()));
                testProcedureLocations = getTestProcedureLocations(filesInPackage);
            }
            return new OdinTestEventsConverter(ODIN_TEST_RUNNER, testProcedureLocations, this);
        }
        throw new IllegalArgumentException("Test kind is not valid");
    }

    @Override
    public @NotNull OdinTestRunConfiguration getConfiguration() {
        return (OdinTestRunConfiguration) super.getConfiguration();
    }
}
