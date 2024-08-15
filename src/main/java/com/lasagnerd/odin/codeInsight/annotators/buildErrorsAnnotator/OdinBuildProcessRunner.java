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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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

        String odinBinaryPath = OdinSdkConfigPersistentState.getOdinBinaryPath(project);

        if (odinBinaryPath == null || !new File(odinBinaryPath).exists()) {
            return null;
        }

        String extraBuildFlags = OdinSdkConfigPersistentState.getInstance(project).extraBuildFlags;
        String filePath = file.getVirtualFile().getPath();

        ProcessBuilder pb = launchProcessBuilder(filePath, odinBinaryPath, extraBuildFlags);
        try {
            Process p = pb.start();

            // Manually consume the error stream, in order to avoid a deadlock situation when buffers are full
            byte[] errorBytes = toByteArray(p.getErrorStream());

            consumeStream(p.getInputStream());

            String stderr = new String(errorBytes);

            p.waitFor(TIMEOUT, TimeUnit.SECONDS);
            if (p.isAlive()) {
                LOG.error("'odin check' did not complete within " + TIMEOUT + " seconds for file " + filePath);
                p.destroy();
            } else {
                int errorCode = p.exitValue();
                if (errorCode == 0) {
                    return null;
                }
            }

            int statusCode = p.exitValue();
            if (statusCode == 0) {
                return null;
            }

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

    private void consumeStream(InputStream inputStream) {
        try {
            while (inputStream.read() != -1) {
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024]; // buffer size of 1KB
        int bytesRead;

        while ((bytesRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        return buffer.toByteArray();
    }

}
