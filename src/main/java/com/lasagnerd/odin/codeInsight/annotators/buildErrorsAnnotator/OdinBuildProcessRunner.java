package com.lasagnerd.odin.codeInsight.annotators.buildErrorsAnnotator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OdinBuildProcessRunner {
    public static Logger LOG = Logger.getInstance(OdinBuildProcessRunner.class);
    private static OdinBuildProcessRunner instance;

    @Nullable
    private OdinBuildErrorResult errors = null;
    public static boolean running = false;

    public static void notify(String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Odin Notifications")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(null);
    }

    public @Nullable OdinBuildErrorResult getErrors() {
        return errors;
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

        if (!Path.of(odinBinaryPath).toFile().exists()) {
            return false;
        }

        String directoryToCompile = config.getDirectoryToCompile();
        if (directoryToCompile == null)
            return false;

        return new File(FileUtil.toSystemDependentName(directoryToCompile)).exists();
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

    private static ProcessBuilder launchProcessBuilder(Project project, String odinBinaryPath, String directoryToCompile) {
        List<String> command = new ArrayList<>();
        command.add(odinBinaryPath);
        command.add("check");
        command.add(".");
        command.add("-json-errors");
        String extraBuildFlags = OdinSdkConfigPersistentState.getInstance(project).extraBuildFlags;
        if (!extraBuildFlags.isEmpty()) {
            Collections.addAll(command, extraBuildFlags.split(" +"));
        }
        return new ProcessBuilder(command).directory(new File(directoryToCompile));
    }

    /**
     * Run {@link #canRunOdinBuild} before calling this method.
     */
    public void buildAndUpdateErrors(Project project) {
        if (running || project == null) {
            return;
        }
        String directoryToCompile = OdinSdkConfigPersistentState.getInstance(project).getDirectoryToCompile();
        String odinBinaryPath = getOdinBinaryPath(project);

        if (odinBinaryPath == null || !new File(odinBinaryPath).exists()) {
            errors = null;
            return;
        }
        ProcessBuilder pb = launchProcessBuilder(project, odinBinaryPath, directoryToCompile);
        try {
            Process p = pb.start();
            int statusCode = p.waitFor();
            if (statusCode == 0) {
                errors = null;
                return;
            }
            String stderr = new String(p.getErrorStream().readAllBytes());
            Gson gson = new GsonBuilder().create();
            try {
                errors = gson.fromJson(stderr, OdinBuildErrorResult.class);
            } catch (JsonSyntaxException e) {
                errors = null;
                LOG.error("Failed to parse errors json", e);
            }
            notify("Done odin build. Found " + errors.getErrors().size() + " errors. " + errors.getErrors().stream().map(OdinBuildErrorResult.ErrorDetails::getMsgs).toList());
        } catch (InterruptedException | IOException e) {
            errors = null;
            LOG.error(e);
        }
    }

}
