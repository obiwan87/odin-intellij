package com.lasagnerd.odin.projectSettings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
