package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.openapi.components.StoredProperty;
import com.lasagnerd.odin.runConfiguration.OdinBaseRunConfigurationOptions;

public class OdinTestRunConfigurationOptions extends OdinBaseRunConfigurationOptions {
    private final StoredProperty<String> testNames = string("").provideDelegate(this, "testNames");

    public String getTestNames() {
        return testNames.getValue(this);
    }

    public void setTestNames(String testNames) {
        this.testNames.setValue(this, testNames);
    }

    private static final String outputPathDefault;

    static {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            outputPathDefault = "bin/$ProjectName$-test.exe";
        } else {
            outputPathDefault = "bin/$ProjectName$-test";
        }
    }

    private final StoredProperty<String> packageDirectoryPath =
            string("").provideDelegate(this, "packageDirectoryPath");
    private final StoredProperty<String> compilerOptions =
            string("").provideDelegate(this, "compilerOptions");

    private final StoredProperty<String> outputPath =
            string(outputPathDefault).provideDelegate(this, "outputPath");

    private final StoredProperty<String> testFilePath =
            string("").provideDelegate(this, "testFilePath");

    private final StoredProperty<String> testKind =
            string("").provideDelegate(this, "testKind");

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

    public String getPackageDirectoryPath() {
        return packageDirectoryPath.getValue(this);
    }

    public void setPackageDirectoryPath(String packageDirectoryPath) {
        this.packageDirectoryPath.setValue(this, packageDirectoryPath);
    }

    public String getOutputPath() {
        return outputPath.getValue(this);
    }

    public void setOutputPath(String outputPath) {
        this.outputPath.setValue(this, outputPath);
    }

    public String getTestKind() {
        return this.testKind.getValue(this);
    }

    public void setTestKind(String testKind) {
        this.testKind.setValue(this, testKind);
    }

    public void setTestFilePath(String testFilePath) {
        this.testFilePath.setValue(this, testFilePath);
    }

    public String getTestFilePath() {
        return this.testFilePath.getValue(this);
    }
}
