package com.lasagnerd.odin.codeInsight.annotators.buildErrorsAnnotator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Getter
public class OdinBuildProcessRunner {
    public static Logger LOG = Logger.getInstance(OdinBuildProcessRunner.class);
    private static OdinBuildProcessRunner instance;

    @Nullable
    private OdinBuildErrorResult buildErrorResult = null;

    @SuppressWarnings("unused")
    public static void notify(String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Odin Notifications")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(null);
    }

    private OdinBuildProcessRunner() {
    }

    public static OdinBuildProcessRunner getInstance() {
        if (instance == null) {
            instance = new OdinBuildProcessRunner();
        }
        return instance;
    }

    public static boolean canRunOdinBuild(Project project) {
        OdinSdkConfigPersistentState config = OdinSdkConfigPersistentState.getInstance(project);
        if (config.sdkPath.isEmpty()) {
            return false;
        }
        String odinBinaryPath = getOdinBinaryPath(project);

        if (odinBinaryPath == null)
            return false;

        return Path.of(odinBinaryPath).toFile().exists();
    }

    @Nullable
    private static String getOdinBinaryPath(Project project) {
        if (project == null) {
            return null;
        }
        OdinSdkConfigPersistentState sdkConfig = OdinSdkConfigPersistentState.getInstance(project);
        if (sdkConfig.getSdkPath() == null)
            return null;

        String systemIndependentPath = FileUtil.toSystemIndependentName(sdkConfig.getSdkPath());
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        return StringUtils.removeEnd(systemIndependentPath, "/") + "/" + "odin" + (isWindows ? ".exe" : "");
    }

    private static ProcessBuilder launchProcessBuilder(String filePath, String odinBinaryPath, String extraBuildFlags) {

        List<String> command = new ArrayList<>();
        command.add(odinBinaryPath);
        command.add("check");
        command.add(filePath);
        command.add("-file");
        command.add("-json-errors");
        command.add("-no-entry-point");
        if (!extraBuildFlags.isEmpty()) {
            Collections.addAll(command, extraBuildFlags.split(" +"));
        }

        return new ProcessBuilder(command).directory(Path.of(filePath).getParent().toFile());
    }

    /**
     * Run {@link #canRunOdinBuild} before calling this method.
     */
    public void buildAndUpdateErrors(Project project, PsiFile file) {
        if (project == null) {
            return;
        }

        String odinBinaryPath = getOdinBinaryPath(project);

        if (odinBinaryPath == null || !new File(odinBinaryPath).exists()) {
            buildErrorResult = null;
            return;
        }

        String extraBuildFlags = OdinSdkConfigPersistentState.getInstance(project).extraBuildFlags;
        String filePath = file.getVirtualFile().getPath();

        ProcessBuilder pb = launchProcessBuilder(filePath, odinBinaryPath, extraBuildFlags);
        try {
            Process p = pb.start();
            boolean exited = p.waitFor(3, TimeUnit.SECONDS);

            if (!exited) {
                LOG.error("odin check did not complete within 3 seconds for file " + filePath);
                buildErrorResult = null;
                return;
            }

            int statusCode = p.exitValue();
            if (statusCode == 0) {
                buildErrorResult = null;
                return;
            }

            String stderr = new String(p.getErrorStream().readAllBytes());
            Gson gson = new GsonBuilder().create();
            try {
                buildErrorResult = gson.fromJson(stderr, OdinBuildErrorResult.class);
            } catch (JsonSyntaxException e) {
                buildErrorResult = null;
                LOG.error("Failed to parse errors json", e);
            }

        } catch (InterruptedException | IOException e) {
            buildErrorResult = null;
            LOG.error(e);
        }
    }

}
