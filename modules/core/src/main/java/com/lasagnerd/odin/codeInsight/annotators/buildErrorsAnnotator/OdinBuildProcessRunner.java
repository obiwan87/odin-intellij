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
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsState;
import com.lasagnerd.odin.settings.projectSettings.OdinSdkUtils;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


@Getter
public class OdinBuildProcessRunner {
    public static final int TIMEOUT = 30;
    public static final Gson GSON = new GsonBuilder().create();
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
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState config = settingsService.getState();

        if (config.sdkPath.isEmpty()) {
            return false;
        }
        String odinBinaryPath = OdinSdkUtils.getOdinBinaryPath(project);

        if (odinBinaryPath == null)
            return false;

        return Path.of(odinBinaryPath).toFile().exists();
    }

    private static ProcessBuilder launchProcessBuilder(Project project, String filePath, String odinBinaryPath, String extraBuildFlags) {

        File fileParentDirPath = Path.of(filePath).getParent().toFile();
        List<String> command = new ArrayList<>();
        command.add(odinBinaryPath);
        command.add("check");
        command.add(fileParentDirPath.toString());
        command.add("-json-errors");
        command.add("-no-entry-point");
        addCollectionPaths(project, filePath, command);

        if (!extraBuildFlags.isEmpty()) {
            Collections.addAll(command, extraBuildFlags.split(" +"));
        }

        return new ProcessBuilder(command).directory(fileParentDirPath);
    }

    public static void addCollectionPaths(Project project, String filePath, List<String> command) {
        Map<String, Path> collectionPaths = OdinImportUtils.getCollectionPaths(project, filePath);
        for (Map.Entry<String, Path> entry : collectionPaths.entrySet()) {
            command.add("-collection:%s=%s".formatted(entry.getKey(), FileUtil.toSystemDependentName(entry.getValue().toString())));
        }
    }

    /**
     * Run {@link #canRunOdinBuild} before calling this method.
     */
    public OdinBuildErrorResult buildAndUpdateErrors(Project project, PsiFile file) {

        if (project == null) {
            return null;
        }

        String odinBinaryPath = OdinSdkUtils.getOdinBinaryPath(project);

        if (odinBinaryPath == null || !new File(odinBinaryPath).exists()) {
            return null;
        }

        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();
        String extraBuildFlags = state.extraBuildFlags;
        String filePath = file.getVirtualFile().getPath();

        ProcessBuilder pb = launchProcessBuilder(project, filePath, odinBinaryPath, extraBuildFlags);
        try {
            Process p = pb.start();

            // Manually consume the error stream, in order to avoid a deadlock situation when buffers are full
            AtomicReference<byte[]> errorBytes = new AtomicReference<>(new byte[0]);

            // Start threads to read output and error streams
            consumeErrorStreamAsync(errorBytes, p);
            consumeOutputStreamAsync(p);

            p.waitFor(TIMEOUT, TimeUnit.SECONDS);

            if (p.isAlive()) {
                LOG.error("'odin check' did not complete within " + TIMEOUT + " seconds for file " + filePath);
                p.destroy();
                return null;
            }

            int statusCode = p.exitValue();
            if (statusCode == 0) {
                return null;
            }

            String stdErr = new String(errorBytes.get());
            try {
                return GSON.fromJson(stdErr, OdinBuildErrorResult.class);
            } catch (JsonSyntaxException e) {
                LOG.warn("Failed to parse errors json", e);
                return null;
            }

        } catch (InterruptedException | IOException e) {
            LOG.warn(e);
            return null;
        }
    }

    private static void consumeOutputStreamAsync(Process p) {
        new Thread(() -> consumeStream(p.getInputStream())).start();
    }

    private static void consumeErrorStreamAsync(AtomicReference<byte[]> errorBytes, Process p) {
        new Thread(() -> {
            try {
                errorBytes.set(toByteArray(p.getErrorStream()));
            } catch (IOException e) {
                LOG.error(e);
            }
        }).start();
    }

    private static void consumeStream(InputStream inputStream) {
        try {
            while (true) {
                if (inputStream.read() == -1) break;
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
