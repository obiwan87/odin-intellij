package com.lasagnerd.odin.codeInsight.annotators.buildAnnotator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.project.Project;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OdinBuildProcessRunner {

    private static OdinBuildProcessRunner instance;

    @Nullable
    private OdinBuildErrorResult errors = null;
    public static boolean running = false;

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
        if (!new File(odinBinaryPath).exists()) {
            return false;
        }
        String directoryToCompile = config.directoryToCompile;
        return new File(directoryToCompile).exists();
    }

    private static String getOdinBinaryPath(Project project) {
        if (project == null) {
            return null;
        }
        OdinSdkConfigPersistentState sdkConfig = OdinSdkConfigPersistentState.getInstance(project);
        String sdkPath = sdkConfig.sdkPath;
        if (!sdkPath.endsWith(File.separator)) {
            sdkPath += File.separator;
        }
        return sdkPath + "odin";
    }

    private static ProcessBuilder launchProcessBuilder(Project project, String odinBinaryPath, String directoryToCompile) {
        List<String> command = new ArrayList<>();
        command.add(odinBinaryPath);
        command.add("build");
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
        if (!new File(odinBinaryPath).exists()) {
            errors = null;
            return;
        }
        ProcessBuilder pb = launchProcessBuilder(project, odinBinaryPath, directoryToCompile);
        try {
            Notifier.notify("Running odin build in " + directoryToCompile);
            Process p = pb.start();
            int statusCode = p.waitFor();
            if (statusCode == 0) {
                Notifier.notify("Done odin build without any errors.");
                errors = null;
                return;
            }
            String stderr = new String(p.getErrorStream().readAllBytes());
            Gson gson = new GsonBuilder().create();
            try {
                errors = gson.fromJson(stderr, OdinBuildErrorResult.class);
            } catch (JsonSyntaxException e) {
                errors = null;
                Notifier.notify("odin build: JsonSyntaxException on " + directoryToCompile + " : " + e.getMessage());
            }
            Notifier.notify("Done odin build. Found " + errors.getErrors().size() + " errors. " + errors.getErrors().stream().map(OdinBuildErrorResult.ErrorDetails::getMsgs).toList());
        } catch (InterruptedException e) {
            errors = null;
            Notifier.notify("odin build: InterruptedException on " + directoryToCompile + " : " + e.getMessage());
        } catch (IOException e) {
            errors = null;
            Notifier.notify("odin build: IOException on " + directoryToCompile + " : " + e.getMessage());
        }
    }

}
