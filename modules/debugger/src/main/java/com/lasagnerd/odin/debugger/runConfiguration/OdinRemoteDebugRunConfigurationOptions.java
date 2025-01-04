package com.lasagnerd.odin.debugger.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class OdinRemoteDebugRunConfigurationOptions extends RunConfigurationOptions {

    private final StoredProperty<String> localPackageDirectoryPath = string("").provideDelegate(this, "localPackageDirectoryPath");
    private final StoredProperty<String> remotePackageDirectoryPath = string("").provideDelegate(this, "remotePackageDirectoryPath");

    private final StoredProperty<String> remoteOdinSdkPath = string("").provideDelegate(this, "remoteOdinSdkPath");

    private final StoredProperty<String> debuggerPath = string("").provideDelegate(this, "debuggerPath");
    private final StoredProperty<String> gdbRemoteArgs = string("").provideDelegate(this, "gdbRemoteArgs");
    private final StoredProperty<String> sshConfigId = string("").provideDelegate(this, "sshConfigId");
    private final StoredProperty<String> localExecutablePath = string("").provideDelegate(this, "localExecutablePath");
    private final StoredProperty<String> targetExecutableUploadDirPath = string("").provideDelegate(this, "targetExecutableUploadDirPath");
    private final StoredProperty<Boolean> buildOnTarget = property(false).provideDelegate(this, "buildOnTarget");
    private final StoredProperty<String> lldbServerPath = string("").provideDelegate(this, "lldbServerPath");
    private final StoredProperty<String> lldbServerArgs = string("").provideDelegate(this, "lldbServerArgs");
    private final StoredProperty<String> targetExecutableDownloadPath = string("").provideDelegate(this, "targetExecutableDownloadPath");
    private final StoredProperty<String> targetExecutableOutputPath = string("").provideDelegate(this, "targetExecutableOutputPath");

    private final StoredProperty<String> programArguments = string("").provideDelegate(this, "programArguments");
    private final StoredProperty<Map<Object, Object>> environmentVariables = map().provideDelegate(this, "environmentVariables");
    private final StoredProperty<String> remoteCompilerOptions = string("").provideDelegate(this, "remoteCompilerOptions");
    private final StoredProperty<String> remoteWorkingDirectory = string("").provideDelegate(this, "remoteWorkingDirectory");
    private final StoredProperty<String> remoteOutputPath = string("").provideDelegate(this, "remoteOutputPath");

    // SSH Config ID
    public String getSshConfigId() {
        return sshConfigId.getValue(this);
    }

    public void setSshConfigId(String sshConfigId) {
        this.sshConfigId.setValue(this, sshConfigId);
    }

    // Local source path
    public String getLocalPackageDirectoryPath() {
        return localPackageDirectoryPath.getValue(this);
    }

    public void setLocalPackageDirectoryPath(String localPackageDirectoryPath) {
        this.localPackageDirectoryPath.setValue(this, localPackageDirectoryPath);
    }

    // Local executable
    public String getLocalExecutablePath() {
        return localExecutablePath.getValue(this);
    }

    public void setLocalExecutablePath(String localExecutablePath) {
        this.localExecutablePath.setValue(this, localExecutablePath);
    }

    // Remote executable
    public String getTargetExecutableUploadDirPath() {
        return targetExecutableUploadDirPath.getValue(this);
    }

    public void setTargetExecutableUploadDirPath(String targetExecutableUploadDirPath) {
        this.targetExecutableUploadDirPath.setValue(this, targetExecutableUploadDirPath);
    }

    // Remote source path
    public String getRemotePackageDirectoryPath() {
        return remotePackageDirectoryPath.getValue(this);
    }

    public void setRemotePackageDirectoryPath(String remoteSourceDirectoryPath) {
        this.remotePackageDirectoryPath.setValue(this, remoteSourceDirectoryPath);
    }


    // Remote Odin SDK path
    public String getRemoteOdinSdkPath() {
        return remoteOdinSdkPath.getValue(this);
    }

    public void setRemoteOdinSdkPath(String remoteOdinSdkPath) {
        this.remoteOdinSdkPath.setValue(this, remoteOdinSdkPath);
    }

    // LLDB debugger path
    public String getDebuggerPath() {
        return debuggerPath.getValue(this);
    }

    public void setDebuggerPath(String debuggerPath) {
        this.debuggerPath.setValue(this, debuggerPath);
    }

    // Build on target
    public boolean isBuildOnTarget() {
        return buildOnTarget.getValue(this);
    }

    public void setBuildOnTarget(boolean buildOnTarget) {
        this.buildOnTarget.setValue(this, buildOnTarget);
    }

    public @Nullable String getLldbServerPath() {
        return this.lldbServerPath.getValue(this);
    }

    // lldb-server Path
    public void setLldbServerPath(@NotNull String lldbServerPath) {
        this.lldbServerPath.setValue(this, lldbServerPath);
    }

    // Target Executable download path
    public @Nullable String getTargetExecutableDownloadPath() {
        return targetExecutableDownloadPath.getValue(this);
    }

    public void setTargetExecutableDownloadPath(@NotNull String targetExecutableDownloadPath) {
        this.targetExecutableDownloadPath.setValue(this, targetExecutableDownloadPath);
    }

    // gdbRemoteArgs
    public @Nullable String getGdbRemoteArgs() {
        return gdbRemoteArgs.getValue(this);
    }

    public void setGdbRemoteArgs(@NotNull String gdbRemoteArgs) {
        this.gdbRemoteArgs.setValue(this, gdbRemoteArgs);
    }

    // lldb-server args
    public @Nullable String getLldbServerArgs() {
        return lldbServerArgs.getValue(this);
    }

    public void setLldbServerArgs(String lldbServerArgs) {
        this.lldbServerArgs.setValue(this, lldbServerArgs);
    }

    // target executable output path
    public @Nullable String getTargetExecutableOutputPath() {
        return targetExecutableOutputPath.getValue(this);
    }

    public void setTargetExecutableOutputPath(String targetExecutableOutputPath) {
        this.targetExecutableOutputPath.setValue(this, targetExecutableOutputPath);
    }


    // program arguments
    public @Nullable String getProgramArguments() {
        return programArguments.getValue(this);
    }

    public void setProgramArguments(String programArguments) {
        this.programArguments.setValue(this, programArguments);
    }

    public @Nullable String getRemoteCompilerOptions() {
        return remoteCompilerOptions.getValue(this);
    }

    public void setRemoteCompilerOptions(String remoteCompilerOptions) {
        this.remoteCompilerOptions.setValue(this, remoteCompilerOptions);
    }

    //environment variables
    public @Nullable Map<Object, Object> getEnvironmentVariables() {
        return environmentVariables.getValue(this);
    }

    public void setEnvironmentVariables(Map<Object, Object> environmentVariables) {
        this.environmentVariables.setValue(this, environmentVariables);
    }

    public @NotNull String getRemoteWorkingDirectory() {
        return remoteWorkingDirectory.getValue(this);
    }

    public void setRemoteWorkingDirectory(@NotNull String remoteWorkingDirectory) {
        this.remoteWorkingDirectory.setValue(this, remoteWorkingDirectory);
    }

    public @NotNull String getRemoteOutputPath() {
        return remoteOutputPath.getValue(this);
    }

    public void setRemoteOutputPath(@NotNull String remoteOutputPath) {
        this.remoteOutputPath.setValue(this, remoteOutputPath);
    }
}
