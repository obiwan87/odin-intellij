package com.lasagnerd.odin.codeInsight.annotators.buildErrorsAnnotator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Getter
public class OdinBuildProcessRunner {
    public static final int TIMEOUT = 10;
    public static Logger LOG = Logger.getInstance(OdinBuildProcessRunner.class);
    private static OdinBuildProcessRunner instance;


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
        String odinBinaryPath = OdinSdkConfigPersistentState.getOdinBinaryPath(project);

        if (odinBinaryPath == null)
            return false;

        return Path.of(odinBinaryPath).toFile().exists();
    }

    private static ProcessBuilder launchProcessBuilder(String filePath, String odinBinaryPath, String extraBuildFlags) {

        File fileParentDirPath = Path.of(filePath).getParent().toFile();
        List<String> command = new ArrayList<>();
        command.add(odinBinaryPath);
        command.add("check");
        command.add(fileParentDirPath.toString());
        command.add("-json-errors");

        if (!extraBuildFlags.isEmpty()) {
            Collections.addAll(command, extraBuildFlags.split(" +"));
        }

        return new ProcessBuilder(command).directory(fileParentDirPath);
    }

    /**
     * Run {@link #canRunOdinBuild} before calling this method.
     */
    public OdinBuildErrorResult buildAndUpdateErrors(Project project, PsiFile file) {

        if (project == null) {
            return null;
        }
        OdinBuildErrorResult buildErrorResult = null;

        String odinBinaryPath = OdinSdkConfigPersistentState.getOdinBinaryPath(project);

        if (odinBinaryPath == null || !new File(odinBinaryPath).exists()) {
            return null;
        }

        String extraBuildFlags = OdinSdkConfigPersistentState.getInstance(project).extraBuildFlags;
        String filePath = file.getVirtualFile().getPath();

        ProcessBuilder pb = launchProcessBuilder(filePath, odinBinaryPath, extraBuildFlags);
        try {
            Process p = pb.start();
            boolean exited = p.waitFor(TIMEOUT, TimeUnit.SECONDS);

            if (!exited) {
                LOG.error("odin check did not complete within " + TIMEOUT + " seconds for file " + filePath);
                return null;
            }

            int statusCode = p.exitValue();
            if (statusCode == 0) {
                return null;
            }

            String stderr = new String(p.getErrorStream().readAllBytes());
            Gson gson = new GsonBuilder().create();
            try {
                return gson.fromJson(stderr, OdinBuildErrorResult.class);
            } catch (JsonSyntaxException e) {
                LOG.error("Failed to parse errors json", e);
                return null;
            }

        } catch (InterruptedException | IOException e) {
            LOG.error(e);
            return null;
        }
    }

}
