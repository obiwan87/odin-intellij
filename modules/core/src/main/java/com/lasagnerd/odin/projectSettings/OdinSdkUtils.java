package com.lasagnerd.odin.projectSettings;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.lasagnerd.odin.codeInsight.annotators.buildErrorsAnnotator.OdinBuildProcessRunner;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdinSdkUtils {
    @Nullable
    public static String getOdinBinaryPath(Project project) {
        if (project == null) {
            return null;
        }
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        String sdkPath = state.getSdkPath();
        if (sdkPath == null)
            return null;

        return getOdinBinaryPath(sdkPath);
    }

    public static @NotNull String getOdinBinaryPath(String sdkPath) {
        String systemIndependentPath = FileUtil.toSystemIndependentName(sdkPath);

        return StringUtils.removeEnd(systemIndependentPath, "/") + "/" + "odin" + (SystemInfo.isWindows ? ".exe" : "");
    }

    public static String getOdinSdkVersion(Project project) {
        Optional<String> sdkPath = getSdkPath(project);
        return sdkPath.map(OdinSdkUtils::getOdinSdkVersion).orElse(null);
    }

    public static @NotNull String getOdinSdkVersion(@NotNull String sdkHome) {
        ProcessBuilder processBuilder = new ProcessBuilder("odin", "version");
        processBuilder.directory(Path.of(sdkHome).toFile());
        try {
            Process start = processBuilder.start();
            byte[] bytes = start.getInputStream().readAllBytes();
            String stdOut = new String(bytes);

            Pattern pattern = Pattern.compile("version (.*)");
            Matcher matcher = pattern.matcher(stdOut);

            String version;
            if (matcher.find()) {
                version = matcher.group(1);

            } else {
                return "Unknown";
            }

            return "Odin version \"" + version + "\"";
        } catch (IOException e) {
            return "Unknown";
        }
    }

    public static @Nullable OdinDebuggerSettings getDebuggerSettings(Project project) {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();
        if (StringUtils.isBlank(state.debuggerId))
            return null;
        return new OdinDebuggerSettings(state.debuggerId, state.debuggerPath);
    }

    public static Optional<String> getSdkPath(Project project) {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        if (settingsService == null) {
            return Optional.empty();
        }

        OdinProjectSettingsState sdkConfig = settingsService.getState();

        String sdkPath = sdkConfig.getSdkPath();
        if (sdkPath == null || sdkPath.isBlank()) {
            return Optional.empty();
        }

        try {
            Path ignore = Path.of(sdkPath);
        } catch (InvalidPathException e) {
            return Optional.empty();
        }

        return Optional.of(sdkPath);
    }

    /**
     * Creates a command line with the currently set odin compiler
     *
     * @param project              Current project
     * @param debug                debug mode?
     * @param mode                 "run" or "debug"
     * @param compilerOptions      compiler options
     * @param outputPathString     output path, i.e. -out:path
     * @param projectDirectoryPath where to build
     * @param programArguments     the arguments to pass to the built executable
     * @param workingDirectory     working directory
     * @return General command line object
     */
    public static @NotNull GeneralCommandLine createCommandLine(Project project,
                                                                boolean debug,
                                                                String mode,
                                                                String compilerOptions,
                                                                String outputPathString,
                                                                String projectDirectoryPath,
                                                                String programArguments,
                                                                String workingDirectory) {
        List<String> command = new ArrayList<>();
        String odinBinaryPath = getOdinBinaryPath(project);

        if (odinBinaryPath == null) {
            throw new RuntimeException("'odin' executable not found. Please setup SDK.");
        }
        String compilerPath = FileUtil.toSystemIndependentName(odinBinaryPath);


        addCommandPart(command, compilerPath);
        addCommandPart(command, mode);

        addCommandPart(command, projectDirectoryPath);
        if (debug) {
            addCommandPart(command, "-debug");
        }
        addCommandPart(command, compilerOptions);
        OdinBuildProcessRunner.addCollectionPaths(project, projectDirectoryPath, command);


        if (!outputPathString.isEmpty()) {
            ProgramParametersConfigurator configurator = new ProgramParametersConfigurator();
            String expandedPath = configurator.expandPathAndMacros(outputPathString, null, project);

            Path outputPath = getAbsolutePath(project, expandedPath);
            if (!outputPath.getParent().toFile().exists()) {
                boolean success = outputPath.getParent().toFile().mkdirs();
                if (!success) {
                    throw new RuntimeException("Failed to create output directory");
                }
            }

            addCommandPart(command, "-out:" + expandedPath);
        }


        if (programArguments != null && !programArguments.isEmpty()) {
            addCommandPart(command, "--");
            addCommandPart(command, programArguments);
        }

        GeneralCommandLine commandLine = new GeneralCommandLine(command);

        commandLine.setWorkDirectory(workingDirectory);

        return commandLine;
    }

    public static Path getAbsolutePath(Project project, String expandedPath) {
        String projectBasePathString = project.getBasePath();
        if (projectBasePathString == null) {
            throw new RuntimeException("Project base path is null");
        }
        Path basePath = Paths.get(projectBasePathString);
        Path outputPathFile = Paths.get(expandedPath);

        return basePath.resolve(outputPathFile);
    }

    public static void addCommandPart(List<String> list, String item) {
        if (item != null && !item.isEmpty()) {
            list.add(item);
        }
    }
}
