package com.lasagnerd.odin.runConfiguration.build;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.runConfiguration.OdinBaseCommandLineState;
import com.lasagnerd.odin.runConfiguration.OdinRunConfigurationUtils;
import com.lasagnerd.odin.settings.projectSettings.OdinSdkUtils;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Setter
@Getter
public class OdinBuildRunCommandLineState extends OdinBaseCommandLineState {
    private final OdinBuildRunConfigurationOptions options;

    public OdinBuildRunCommandLineState(@NotNull ExecutionEnvironment environment,
                                        @NotNull OdinBuildRunConfigurationOptions options) {
        super(environment);
        this.options = options;
    }

    public @NotNull GeneralCommandLine createCommandLine(boolean debug) {
        ExecutionEnvironment environment = getEnvironment();
        Project project = environment.getProject();
        OdinRunConfigurationUtils.OdinToolMode mode;

        if (options.isRunAfterBuild()) {
            if (debug) {
                mode = OdinRunConfigurationUtils.OdinToolMode.BUILD;
            } else {
                mode = OdinRunConfigurationUtils.OdinToolMode.RUN;
            }
        } else {
            mode = OdinRunConfigurationUtils.OdinToolMode.BUILD;
        }

        String buildPath = options.getPackageDirectoryPath();
        String programArguments;
        if (mode == OdinRunConfigurationUtils.OdinToolMode.RUN) {
            programArguments = options.getProgramArguments();
        } else {
            programArguments = null;
        }
        String compilerOptions = Objects.requireNonNullElse(options.getCompilerOptions(), "");
        String outputPathString = Objects.requireNonNullElse(options.getOutputPath(), "");

        if (options.getBuildAsFile()) {
            compilerOptions = "-file " + compilerOptions;
        }

        String basePath = project.getBasePath();
        String workingDirectory;
        if (options.getWorkingDirectory() != null) {
            workingDirectory = options.getWorkingDirectory();
        } else {
            workingDirectory = basePath;
        }

        return OdinRunConfigurationUtils.createCommandLine(project, OdinSdkUtils.getOdinBinaryPath(project), debug,
                mode,
                compilerOptions,
                outputPathString,
                buildPath,
                programArguments,
                workingDirectory);
    }
}
