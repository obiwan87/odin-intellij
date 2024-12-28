package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.lang.psi.OdinConstantInitDeclaration;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public class OdinSMTRunnerConsoleProperties extends SMTRunnerConsoleProperties implements SMCustomMessagesParsing {

    public static final String ODIN_TEST_RUNNER = "Odin Test Runner";

    public OdinSMTRunnerConsoleProperties(@NotNull RunConfiguration config, @NotNull String testFrameworkName, @NotNull Executor executor) {
        super(config, testFrameworkName, executor);
    }

    public OdinSMTRunnerConsoleProperties(@NotNull Project project, @NotNull RunProfile config, @NotNull String testFrameworkName, @NotNull Executor executor) {
        super(project, config, testFrameworkName, executor);
    }


    @Override
    public OutputToGeneralTestEventsConverter createTestEventsConverter(@NotNull String testFrameworkName, @NotNull TestConsoleProperties consoleProperties) {
        OdinTestRunConfiguration configuration = getConfiguration();
        OdinTestRunConfigurationOptions options = configuration.getOptions();
        if (Objects.equals(options.getTestKind(), "Package") || Objects.equals(options.getTestKind(), "File")) {
            return new OdinTestEventsConverter(ODIN_TEST_RUNNER, this);
        }
        throw new IllegalArgumentException("Test kind is not valid");
    }

    private static @NotNull OdinSMTRunnerConsoleProperties.TestProceduresLocations getTestProcedureLocations(List<OdinFile> filesInPackage) {
        Map<String, Path> procedureToFilePath = new HashMap<>();
        Map<Path, List<String>> fileToProcedureName = new HashMap<>();
        for (OdinFile file : filesInPackage) {
            VirtualFile containingVirtualFile = OdinImportUtils.getContainingVirtualFile(file);
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
        return new TestProceduresLocations(procedureToFilePath, fileToProcedureName);
    }

    private record TestProceduresLocations(Map<String, Path> procedureToFilePath, Map<Path, List<String>> fileToProcedureName) {
    }

    @Override
    public @NotNull OdinTestRunConfiguration getConfiguration() {
        return (OdinTestRunConfiguration) super.getConfiguration();
    }
}
