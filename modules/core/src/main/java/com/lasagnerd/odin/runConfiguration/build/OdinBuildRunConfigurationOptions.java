package com.lasagnerd.odin.runConfiguration.build;

import com.intellij.openapi.components.StoredProperty;
import com.lasagnerd.odin.runConfiguration.OdinBaseRunConfigurationOptions;

public class OdinBuildRunConfigurationOptions extends OdinBaseRunConfigurationOptions {
    public static final String OUTPUT_PATH_DEFAULT;

    static {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            OUTPUT_PATH_DEFAULT = "bin/$ProjectName$.exe";
        } else {
            OUTPUT_PATH_DEFAULT = "bin/$ProjectName$";
        }
    }

    private final StoredProperty<String> projectDirectoryPath =
            string("").provideDelegate(this, "projectDirectoryPath");
    private final StoredProperty<String> compilerOptions =
            string("").provideDelegate(this, "compilerOptions");

    private final StoredProperty<String> outputPath =
            string(OUTPUT_PATH_DEFAULT).provideDelegate(this, "outputPath");

    private final StoredProperty<String> workingDirectory =
            string("$ProjectFileDir$").provideDelegate(this, "workingDirectory");

    private final StoredProperty<String> programArguments = string("").provideDelegate(this, "programArguments");

    private final StoredProperty<Boolean> runAfterBuild = property(true).provideDelegate(this, "runAfterBuild");

    @Override
    public String getWorkingDirectory() {
        return workingDirectory.getValue(this);
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory.setValue(this, workingDirectory);
    }

    @Override
    public String getProgramArguments() {
        return programArguments.getValue(this);
    }

    @Override
    public void setProgramArguments(String programArguments) {
        this.programArguments.setValue(this, programArguments);
    }

    @Override
    public String getCompilerOptions() {
        return compilerOptions.getValue(this);
    }

    @Override
    public void setCompilerOptions(String compilerOptions) {
        this.compilerOptions.setValue(this, compilerOptions);
    }

    @Override
    public String getPackageDirectoryPath() {
        return getProjectDirectoryPath();
    }

    @Override
    public void setPackageDirectoryPath(String packageDirectoryPath) {
        setProjectDirectoryPath(packageDirectoryPath);
    }

    public String getProjectDirectoryPath() {
        return projectDirectoryPath.getValue(this);
    }

    public void setProjectDirectoryPath(String projectDirectoryPath) {
        this.projectDirectoryPath.setValue(this, projectDirectoryPath);
    }

    @Override
    public String getOutputPath() {
        return outputPath.getValue(this);
    }

    @Override
    public void setOutputPath(String outputPath) {
        this.outputPath.setValue(this, outputPath);
    }

    public boolean isRunAfterBuild() {
        return this.runAfterBuild.getValue(this);
    }

    public void setRunAfterBuild(boolean runAfterBuild) {
        this.runAfterBuild.setValue(this, runAfterBuild);
    }
}
