package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class OdinRunConfigurationOptions extends RunConfigurationOptions {
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
