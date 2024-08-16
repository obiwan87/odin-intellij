package com.lasagnerd.odin.sdkConfig;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdinSdkUtils {
    @Nullable
    public static String getOdinBinaryPath(Project project) {
        if (project == null) {
            return null;
        }
        OdinSdkConfigPersistentState sdkConfig = OdinSdkConfigPersistentState.getInstance(project);
        String sdkPath = sdkConfig.getSdkPath();
        if (sdkPath == null)
            return null;

        return getOdinBinaryPath(sdkPath);
    }

    public static @NotNull String getOdinBinaryPath(String sdkPath) {
        String systemIndependentPath = FileUtil.toSystemIndependentName(sdkPath);
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        return StringUtils.removeEnd(systemIndependentPath, "/") + "/" + "odin" + (isWindows ? ".exe" : "");
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


    public static Optional<String> getSdkPath(Project project) {
        OdinSdkConfigPersistentState sdkConfig = OdinSdkConfigPersistentState.getInstance(project);
        if (sdkConfig == null) {
            return Optional.empty();
        }

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
}
