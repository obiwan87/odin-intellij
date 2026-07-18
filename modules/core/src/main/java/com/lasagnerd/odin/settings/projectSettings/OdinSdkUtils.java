package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
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
        return OdinProjectToolchainService.getInstance(project).getCompilerPath().orElse(null);
    }

    public static @NotNull String getOdinBinaryPath(String sdkPath) {
        String systemIndependentPath = FileUtil.toSystemIndependentName(sdkPath);

        String s = Strings.CS.removeEnd(systemIndependentPath, "/");
        return s + "/" + "odin" + (SystemInfo.isWindows ? ".exe" : "");
    }

    public static String getOdinSdkVersion(Project project) {
        return OdinProjectToolchainService.getInstance(project).getCompilerPath()
                .map(OdinSdkUtils::getOdinCompilerVersion).orElse(null);
    }

    public static @NotNull String getOdinSdkVersion(@NotNull String sdkHome) {
        return getOdinCompilerVersion(getOdinBinaryPath(sdkHome));
    }

    public static @NotNull String getOdinCompilerVersion(@NotNull String compilerPath) {
        ProcessBuilder processBuilder = new ProcessBuilder(compilerPath, "version");

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
        return OdinProjectToolchainService.getInstance(project).getDebuggerSettings();
    }

    public static Optional<String> getCompilerPath(Project project) {
        return OdinProjectToolchainService.getInstance(project).getCompilerPath();
    }

    public static Optional<String> getLibraryPath(Project project) {
        return OdinProjectToolchainService.getInstance(project).getLibraryPath();
    }

    /** @deprecated The old SDK path is the library root in the split toolchain model. */
    @Deprecated
    public static Optional<String> getSdkPath(Project project) {
        return getLibraryPath(project);
    }

    public static Optional<String> getValidSdkPath(Project project) {
        String odinBinaryPath = getOdinBinaryPath(project);
        if (odinBinaryPath != null) {
            if (new File(odinBinaryPath).exists()) {
                return getLibraryPath(project);
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
