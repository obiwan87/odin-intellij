package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
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
import com.lasagnerd.odin.projectSettings.OdinSdkUtils;
import com.lasagnerd.odin.runConfiguration.OdinBaseCommandLineState;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class OdinTestRunCommandLineState extends OdinBaseCommandLineState {

    @NotNull
    private final OdinTestRunConfigurationOptions options;

    protected OdinTestRunCommandLineState(ExecutionEnvironment environment, @NotNull OdinTestRunConfigurationOptions options) {
        super(environment);
        this.options = options;
    }

    private String getTestNames() {
        AtomicReference<String> testNames = new AtomicReference<>("");
        if (Objects.equals(options.getTestKind(), "File")) {
            ApplicationManager.getApplication().runReadAction(() -> {
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
                        testNames.set(String.join(",", names));
                    }
                }
            });
        } else if (Objects.equals(options.getTestKind(), "Package")) {
            testNames.set(options.getTestNames());
        }
        return testNames.get();
    }

    @Override
    @NotNull
    public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
        ProcessHandler processHandler = this.startProcess();
        ConsoleView consoleView = createConsoleAndAttach(processHandler);
        return new DefaultExecutionResult(consoleView, processHandler);
    }

    public ConsoleView createConsoleAndAttach(ProcessHandler processHandler) {
        OdinTestRunConfiguration profile = (OdinTestRunConfiguration) getEnvironment().getRunProfile();

        ConsoleView testsOutputConsoleView = createConsole(profile);
        testsOutputConsoleView.attachToProcess(processHandler);
        return testsOutputConsoleView;
    }

    public @NotNull ConsoleView createConsole(OdinTestRunConfiguration profile) {
        return SMTestRunnerConnectionUtil
                .createConsole("Odin Test Runner",
                        new OdinSMTRunnerConsoleProperties(profile,
                                "Odin Test Runner",
                                getEnvironment().getExecutor()));
    }

    @Override
    public @NotNull GeneralCommandLine createCommandLine(boolean debug) {

        OdinRunConfigurationUtils.OdinToolMode toolMode = debug ? OdinRunConfigurationUtils.OdinToolMode.BUILD : OdinRunConfigurationUtils.OdinToolMode.TEST;

        GeneralCommandLine commandLine = OdinRunConfigurationUtils.createCommandLine(getEnvironment().getProject(),
                OdinSdkUtils.getOdinBinaryPath(getEnvironment().getProject()), false,
                toolMode,
                options.getCompilerOptions(),
                options.getOutputPath(),
                options.getPackageDirectoryPath(),
                null,
                options.getWorkingDirectory());


        String testNames = getTestNames();

        if (testNames != null && !testNames.isBlank()) {
            commandLine.addParameter("-define:ODIN_TEST_NAMES=" + testNames);
        }

        if (debug) {
            commandLine.addParameter("-build-mode:test");
        }

        commandLine.addParameter("-define:ODIN_TEST_THREADS=1");
        commandLine.addParameter("-define:ODIN_TEST_FANCY=false");
        commandLine.addParameter("-debug");

        return commandLine;
    }
}
