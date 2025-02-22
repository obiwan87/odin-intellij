package com.lasagnerd.odin.runConfiguration.build;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.execution.impl.CheckableRunConfigurationEditor;
import com.intellij.ide.macro.MacrosDialog;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinBuildRunConfigurationSettingsEditor extends SettingsEditor<OdinBuildRunConfiguration> implements CheckableRunConfigurationEditor<OdinBuildRunConfiguration> {

    private final JPanel panel;
    private final JTextField compilerOptions;
    private final ExtendableTextField workingDirectory;
    private final RawCommandLineEditor programArguments;
    private final ExtendableTextField packageRootPathTextField;
    private final ExtendableTextField outputPath;
    private final JBCheckBox runAfterBuildCheckbox;
    private final JBCheckBox buildAsFileCheckbox;

    public OdinBuildRunConfigurationSettingsEditor(@NotNull Project project) {
        // Get project
        compilerOptions = new JTextField("run .");

        outputPath = new ExtendableTextField("$ProjectFileDir$/bin");
        outputPath.setColumns(0);
        MacrosDialog.addMacroSupport(outputPath, (x) -> true, () -> false);

        packageRootPathTextField = new ExtendableTextField("$ProjectFileDir$");
        TextFieldWithBrowseButton packageRootPath = createDirectoryChooser(project, this.packageRootPathTextField);
        MacrosDialog.addMacroSupport(this.packageRootPathTextField, (x) -> true, () -> false);

        workingDirectory = new ExtendableTextField("$ProjectFileDir$");
        TextFieldWithBrowseButton extendableWorkingDirectory = createDirectoryChooser(project, workingDirectory);
        MacrosDialog.addMacroSupport(workingDirectory, (x) -> true, () -> false);

        EnvironmentVariablesTextFieldWithBrowseButton environmentVariables = new EnvironmentVariablesTextFieldWithBrowseButton();

        programArguments = new RawCommandLineEditor();
        MacrosDialog.addMacroSupport(programArguments.getEditorField(), (x) -> true, () -> false);

        runAfterBuildCheckbox = new JBCheckBox();

        buildAsFileCheckbox = new JBCheckBox();

        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Build as file", buildAsFileCheckbox)
                .addComponentToRightColumn(
                        OdinProjectSettings.createComment("Prepends '-file' to compiler options.")
                )
                .addLabeledComponent("Build path", packageRootPath)
                .addLabeledComponent("Program arguments", programArguments)
                .addLabeledComponent("Working directory", extendableWorkingDirectory)
                .addLabeledComponent("Compiler options", compilerOptions, 15)
                .addLabeledComponent("Output path", outputPath)
                .addLabeledComponent("Run after build?", runAfterBuildCheckbox)
                .addLabeledComponent("Environment variables", environmentVariables)

                .getPanel();
    }

    @NotNull
    private TextFieldWithBrowseButton createDirectoryChooser(@NotNull Project project, JTextField textField) {
        return new TextFieldWithBrowseButton(textField, e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(
                    FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor(OdinFileType.INSTANCE),
                    project,
                    null);
            if (virtualFile == null) return;
            textField.setText(virtualFile.getPath());
        }, null);
    }

    @Override
    protected void resetEditorFrom(@NotNull OdinBuildRunConfiguration s) {
        compilerOptions.setText(s.getOptions().getCompilerOptions());
        packageRootPathTextField.setText(s.getOptions().getPackageDirectoryPath());
        outputPath.setText(s.getOptions().getOutputPath());
        workingDirectory.setText(s.getOptions().getWorkingDirectory());
        programArguments.setText(s.getOptions().getProgramArguments());
        runAfterBuildCheckbox.setSelected(s.getOptions().isRunAfterBuild());
        buildAsFileCheckbox.setSelected(s.getOptions().getBuildAsFile());
    }

    @Override
    protected void applyEditorTo(@NotNull OdinBuildRunConfiguration s) {
        s.getOptions().setCompilerOptions(compilerOptions.getText());
        s.getOptions().setPackageDirectoryPath(packageRootPathTextField.getText());
        s.getOptions().setOutputPath(outputPath.getText());
        s.getOptions().setWorkingDirectory(workingDirectory.getText());
        s.getOptions().setProgramArguments(programArguments.getText());
        s.getOptions().setRunAfterBuild(runAfterBuildCheckbox.isSelected());
        s.getOptions().setBuildAsFile(buildAsFileCheckbox.isSelected());
    }


    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }

    @Override
    public void checkEditorData(OdinBuildRunConfiguration s) {

    }
}
