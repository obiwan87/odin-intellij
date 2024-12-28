package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.configurations.LocatableRunConfigurationOptions;

public abstract class OdinBaseRunConfigurationOptions extends LocatableRunConfigurationOptions {
    public abstract String getWorkingDirectory();

    public abstract void setWorkingDirectory(String workingDirectory);

    public abstract String getProgramArguments();

    public abstract void setProgramArguments(String programArguments);

    public abstract String getCompilerOptions();

    public abstract void setCompilerOptions(String compilerOptions);

    public abstract String getPackageDirectoryPath();

    public abstract void setPackageDirectoryPath(String projectDirectoryPath);

    public abstract String getOutputPath();

    public abstract void setOutputPath(String outputPath);
}
