package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class OdinRunConfigurationOptions extends RunConfigurationOptions {
    private final StoredProperty<String> filePath =
            string("").provideDelegate(this, "filePath");
    private final StoredProperty<String> compilerOptions =
            string("").provideDelegate(this, "compilerOptions");

    public String getCompilerOptions() {
        return compilerOptions.getValue(this);
    }

    public void setCompilerOptions(String compilerOptions) {
        this.compilerOptions.setValue(this, compilerOptions);
    }

    public String getFilePath() {
        return filePath.getValue(this);
    }

    public void setFilePath(String filePath) {
        this.filePath.setValue(this, filePath);
    }
}
