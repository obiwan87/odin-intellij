package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.configurations.LocatableRunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public abstract class OdinBaseRunConfigurationOptions extends LocatableRunConfigurationOptions {
    private final StoredProperty<Boolean> emulateTerminal =
            property(false).provideDelegate(this, "emulateTerminal");

    public boolean isEmulateTerminal() {
        return emulateTerminal.getValue(this);
    }

    public void setEmulateTerminal(boolean emulateTerminal) {
        this.emulateTerminal.setValue(this, emulateTerminal);
    }

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
