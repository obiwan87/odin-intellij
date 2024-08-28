package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.configurations.LocatableRunConfigurationOptions;
import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class OdinRunConfigurationOptions extends LocatableRunConfigurationOptions {
    private static final String outputPathDefault;

    static {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            outputPathDefault = "bin/$ProjectName$.exe";
        } else {
            outputPathDefault = "bin/$ProjectName$";
        }
    }

    private final StoredProperty<String> projectDirectoryPath =
            string("").provideDelegate(this, "projectDirectoryPath");
    private final StoredProperty<String> compilerOptions =
            string("").provideDelegate(this, "compilerOptions");

    private final StoredProperty<String> outputPath =
            string(outputPathDefault).provideDelegate(this, "outputPath");

    private final StoredProperty<String> workingDirectory =
            string("$ProjectFileDir$").provideDelegate(this, "workingDirectory");

    private final StoredProperty<String> programArguments = string("").provideDelegate(this, "programArguments");

    public String getWorkingDirectory() {
        return workingDirectory.getValue(this);
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory.setValue(this, workingDirectory);
    }

    public String getProgramArguments() {
        return programArguments.getValue(this);
    }

    public void setProgramArguments(String programArguments) {
        this.programArguments.setValue(this, programArguments);
    }

    public String getCompilerOptions() {
        return compilerOptions.getValue(this);
    }

    public void setCompilerOptions(String compilerOptions) {
        this.compilerOptions.setValue(this, compilerOptions);
    }

    public String getProjectDirectoryPath() {
        return projectDirectoryPath.getValue(this);
    }

    public void setProjectDirectoryPath(String projectDirectoryPath) {
        this.projectDirectoryPath.setValue(this, projectDirectoryPath);
    }

    public String getOutputPath() {
        return outputPath.getValue(this);
    }

    public void setOutputPath(String outputPath) {
        this.outputPath.setValue(this, outputPath);
    }
}
