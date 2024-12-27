package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.lang.psi.OdinConstantInitDeclaration;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.projectSettings.OdinProjectConfigurable;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsState;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OdinTestRunConfiguration extends LocatableConfigurationBase<OdinTestRunConfigurationOptions>
        implements RunProfileWithCompileBeforeLaunchOption {
    protected OdinTestRunConfiguration(@NotNull Project project,
                                       @NotNull ConfigurationFactory factory,
                                       @Nullable String name) {
        super(project, factory, name);
    }

    private String expandPath(String s) {
        ProgramParametersConfigurator configurator = new ProgramParametersConfigurator();
        return configurator.expandPathAndMacros(s, null, getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        expandAndCheck(getOptions().getPackageDirectoryPath(), "Project directory");
        expandAndCheck(getOptions().getWorkingDirectory(), "Working directory");
        checkSet(getOptions().getOutputPath(), "Output path");

        OdinProjectSettingsState state = OdinProjectSettingsService.getInstance(getProject()).getState();
        String sdkPath = state.sdkPath;
        if (sdkPath == null || sdkPath.isEmpty()) {
            throw new RuntimeConfigurationError("Odin SDK path is not set",
                    () -> ShowSettingsUtil.getInstance().showSettingsDialog(getProject(),
                            OdinProjectConfigurable.class, null));
        }

        File sdkFile = new File(sdkPath);
        if (!sdkFile.exists()) {
            throw new RuntimeConfigurationError("Odin SDK path does not exist");
        }
    }

    private void expandAndCheck(String path, String label) throws RuntimeConfigurationError {
        path = expandPath(path);
        checkSet(path, label);
        checkExists(path, label);
    }

    private static void checkExists(String projectDirectoryPath, String label) throws RuntimeConfigurationError {
        File file = new File(projectDirectoryPath);
        if (!file.exists()) {
            throw new RuntimeConfigurationError(label + " does not exist");
        }
    }

    private static void checkSet(String projectDirectoryPath, String label) throws RuntimeConfigurationError {
        if (projectDirectoryPath == null || projectDirectoryPath.isEmpty()) {
            throw new RuntimeConfigurationError(label + " is not set");
        }
    }

    @Override
    @NotNull
    public OdinTestRunConfigurationOptions getOptions() {
        return (OdinTestRunConfigurationOptions) super.getOptions();
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new OdinTestRunConfigurationSettingsEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new OdinTestrunCommandLineState(environment, getOptions());
    }

    public @NotNull String getOutputPath() {
        return getOptions().getOutputPath();
    }

    private static class OdinTestrunCommandLineState extends CommandLineState {

        @NotNull
        private final OdinTestRunConfigurationOptions options;

        protected OdinTestrunCommandLineState(ExecutionEnvironment environment, @NotNull OdinTestRunConfigurationOptions options) {
            super(environment);
            this.options = options;
        }

        @Override
        protected @NotNull ProcessHandler startProcess() throws ExecutionException {

            GeneralCommandLine commandLine = OdinRunConfigurationUtils.createCommandLine(getEnvironment().getProject(),
                    false,
                    OdinRunConfigurationUtils.OdinToolMode.TEST,
                    options.getCompilerOptions(),
                    options.getOutputPath(),
                    options.getPackageDirectoryPath(),
                    null,
                    options.getWorkingDirectory());


            String testNames = getTestNames();

            if (testNames != null && !testNames.isBlank()) {
                commandLine.addParameter("-define:ODIN_TEST_NAMES=" + testNames);
            }
            commandLine.addParameter("-define:ODIN_TEST_THREADS=1");
            commandLine.addParameter("-define:ODIN_TEST_FANCY=false");
            commandLine.addParameter("-debug");
            OSProcessHandler processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);
            ProcessTerminatedListener.attach(processHandler);

            return processHandler;
        }

        private String getTestNames() {
            String testNames = "";
            if (Objects.equals(options.getTestKind(), "File")) {
                PsiManager psiManager = PsiManager.getInstance(getEnvironment().getProject());
                VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(options.getTestFilePath()));
                if (virtualFile != null) {
                    PsiFile file = psiManager.findFile(virtualFile);
                    if (file instanceof OdinFile odinFile) {
                        String packageName = odinFile.getFileScope().getPackageClause().getName();
                        OdinSymbolTable symbolTable = odinFile.getFileScope().getFullSymbolTable();
                        List<String> names = new ArrayList<>();
                        for (OdinSymbol symbol : symbolTable.getSymbols()) {
                            if (symbol.getSymbolType() == OdinSymbolType.PROCEDURE) {
                                OdinDeclaration declaration = symbol.getDeclaration();
                                if (declaration instanceof OdinConstantInitDeclaration constantInitDeclaration &&
                                        OdinInsightUtils.containsAttribute(constantInitDeclaration.getAttributesDefinitionList(), "test")) {
                                    names.add(packageName + "." + symbol.getName());
                                }
                            }
                        }
                        testNames = String.join(",", names);
                    }
                }
            } else if (Objects.equals(options.getTestKind(), "Package")) {
                testNames = options.getTestNames();
            }
            return testNames;
        }


        @Override
        @NotNull
        public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
            ProcessHandler processHandler = this.startProcess();
            ConsoleView consoleView = createConsole(processHandler);
            return new DefaultExecutionResult(consoleView, processHandler);
        }

        public ConsoleView createConsole(ProcessHandler processHandler) {
            OdinTestRunConfiguration profile = (OdinTestRunConfiguration) getEnvironment().getRunProfile();

            BaseTestsOutputConsoleView testsOutputConsoleView = SMTestRunnerConnectionUtil
                    .createConsole("Odin Test Runner",
                            new OdinSMTRunnerConsoleProperties(profile,
                                    "Odin Test Runner",
                                    getEnvironment().getExecutor()));
            testsOutputConsoleView.attachToProcess(processHandler);
            return testsOutputConsoleView;
        }


    }

}
