package com.lasagnerd.odin.projectSettings;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.lasagnerd.odin.codeInsight.annotators.buildErrorsAnnotator.OdinBuildProcessRunner;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
        Path path = Path.of(sdkHome, "odin");
        ProcessBuilder processBuilder = new ProcessBuilder(path.toString(), "version");

        try {
            Process start = processBuilder.start();
            byte[] bytes = start.getInputStream().readAllBytes();
            String stdOut = new String(bytes);

            Pattern pattern = Pattern.compile("version (.*)");
            Matcher matcher = pattern.matcher(stdOut);

            String version;
            if (matcher.find()) {
                version = matcher.group(1).trim();
            } else {
                return "Unknown";
            }

            return version;
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

    public static Optional<String> getValidSdkPath(Project project) {
        String odinBinaryPath = getOdinBinaryPath(project);
        if (odinBinaryPath != null) {
            if (new File(odinBinaryPath).exists()) {
                return getSdkPath(project);
            }
        }
        return Optional.empty();
    }

    public static VirtualFile getValidSdkPathFile(Project project) {
        Optional<String> validSdkPath = getValidSdkPath(project);
        return validSdkPath
                .map(s -> VirtualFileManager.getInstance().findFileByNioPath(Path.of(s)))
                .orElse(null);
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
        ProgramParametersConfigurator configurator = new ProgramParametersConfigurator();
        Function<String, String> expandPath = s -> configurator.expandPathAndMacros(s, null, project);

        projectDirectoryPath = expandPath.apply(projectDirectoryPath);
        workingDirectory = expandPath.apply(workingDirectory);
        outputPathString = expandPath.apply(outputPathString);

        List<String> command = new ArrayList<>();
        String odinBinaryPath = getOdinBinaryPath(project);

        if (odinBinaryPath == null) {
            throw new RuntimeException("'odin' executable not found. Please setup SDK.");
        }
        String compilerPath = FileUtil.toSystemIndependentName(odinBinaryPath);


        addCommandPart(command, compilerPath);
        addCommandPart(command, mode);

        addCommandPart(command, projectDirectoryPath);

        Collections.addAll(command, compilerOptions.split(" +"));
        if (debug && !command.contains("-debug")) {
            addCommandPart(command, "-debug");
        }
        OdinBuildProcessRunner.addCollectionPaths(project, projectDirectoryPath, command);

        if (!outputPathString.isEmpty()) {

            Path outputPath = getAbsolutePath(project, outputPathString);
            if (!outputPath.getParent().toFile().exists()) {
                boolean success = outputPath.getParent().toFile().mkdirs();
                if (!success) {
                    throw new RuntimeException("Failed to create output directory");
                }
            }

            addCommandPart(command, "-out:" + outputPathString);
        }

        if (programArguments != null && !programArguments.isEmpty()) {
            addCommandPart(command, "--");
            Collections.addAll(command, programArguments.split(" +"));
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
