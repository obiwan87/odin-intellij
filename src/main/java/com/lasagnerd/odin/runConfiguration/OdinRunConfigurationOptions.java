package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class OdinRunConfigurationOptions extends RunConfigurationOptions {
    private final StoredProperty<String> projectDirectoryPath =
            string("").provideDelegate(this, "projectDirectoryPath");
    private final StoredProperty<String> compilerOptions =
            string("").provideDelegate(this, "compilerOptions");

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
}
