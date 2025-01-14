package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.execution.impl.CheckableRunConfigurationEditor;
import com.intellij.ide.macro.MacrosDialog;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static com.lasagnerd.odin.settings.projectSettings.OdinProjectSettings.createGridBagConstraintsForFirstColumn;

public class OdinTestRunConfigurationSettingsEditor extends SettingsEditor<OdinTestRunConfiguration> implements CheckableRunConfigurationEditor<OdinTestRunConfiguration> {

    private final JPanel panel;
    private final RawCommandLineEditor compilerOptions;
    private final ExtendableTextField workingDirectory;
    private final RawCommandLineEditor programArguments;
    private final ExtendableTextField packageDirectoryPath;
    private final ExtendableTextField outputPath;
    private final JTextField testNames;
    private final ComboBox<String> testKindComboBox;
    private final JPanel testKindPanel;
    private final TextFieldWithBrowseButton fileField;
    private final JPanel fileTestPanel;
    private final JPanel testNamesPanel;

    public OdinTestRunConfigurationSettingsEditor(@NotNull Project project) {
        // Get project
        compilerOptions = new RawCommandLineEditor();

        testKindComboBox = new ComboBox<>();
        testKindComboBox.addItem("File");
        testKindComboBox.addItem("Package");

        testKindComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateTestKindPanel(e.getItem());
            }
        });

        fileField = new TextFieldWithBrowseButton(new ExtendableTextField(10));
        testNames = new JTextField();

        fileTestPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("File", fileField)
                .setFormLeftIndent(20)
                .getPanel();

        testNamesPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Test names", testNames)
                .setFormLeftIndent(20)
                .getPanel();

        testKindPanel = FormBuilder.createFormBuilder()
                .addComponent(fileTestPanel)
                .getPanel();

        outputPath = new ExtendableTextField("$ProjectFileDir$/bin");
        outputPath.setColumns(0);
        MacrosDialog.addMacroSupport(outputPath, (x) -> true, () -> false);

        packageDirectoryPath = new ExtendableTextField("$ProjectFileDir$");
        TextFieldWithBrowseButton extendableProjectDirectoryPath = createDirectoryChooser(project, packageDirectoryPath);
        MacrosDialog.addMacroSupport(packageDirectoryPath, (x) -> true, () -> false);

        workingDirectory = new ExtendableTextField("$ProjectFileDir$");
        TextFieldWithBrowseButton extendableWorkingDirectory = createDirectoryChooser(project, workingDirectory);
        MacrosDialog.addMacroSupport(workingDirectory, (x) -> true, () -> false);

        EnvironmentVariablesTextFieldWithBrowseButton environmentVariables = new EnvironmentVariablesTextFieldWithBrowseButton();

        programArguments = new RawCommandLineEditor();
        MacrosDialog.addMacroSupport(programArguments.getEditorField(), (x) -> true, () -> false);


        JLabel label = new JLabel(UIUtil.replaceMnemonicAmpersand("File"));
        label.setLabelFor(testKindPanel);

        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Test kind", testKindComboBox)
                .addComponent(testKindPanel)
                .addLabeledComponent("Package directory", extendableProjectDirectoryPath)
                .addLabeledComponent("Program arguments", programArguments)
                .addLabeledComponent("Working directory", extendableWorkingDirectory)
                .addLabeledComponent("Compiler options", compilerOptions, 15)
                .addLabeledComponent("Output path", outputPath)
                .addLabeledComponent("Environment variables", environmentVariables)
                .setFormLeftIndent(20)
                .getPanel();
    }

    private void updateTestKindPanel(Object item) {
        if (testKindPanel.getComponentCount() > 0) {
            testKindPanel.remove(0);
        }
        if (item.equals("File")) {
            testKindPanel.add(fileTestPanel, createGridBagConstraintsForFirstColumn());
        }

        if (item.equals("Package")) {
            testKindPanel.add(testNamesPanel, createGridBagConstraintsForFirstColumn());
        }
    }

    @NotNull
    private TextFieldWithBrowseButton createDirectoryChooser(@NotNull Project project, JTextField textField) {
        return new TextFieldWithBrowseButton(textField, e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    project,
                    null);
            if (virtualFile == null) return;
            textField.setText(virtualFile.getPath());
        }, null);
    }

    @Override
    protected void resetEditorFrom(@NotNull OdinTestRunConfiguration s) {
        testKindComboBox.setSelectedItem(s.getOptions().getTestKind());
        fileField.setText(s.getOptions().getTestFilePath());
        updateTestKindPanel(s.getOptions().getTestKind());

        compilerOptions.setText(s.getOptions().getCompilerOptions());
        packageDirectoryPath.setText(s.getOptions().getPackageDirectoryPath());
        testNames.setText(s.getOptions().getTestNames());
        outputPath.setText(s.getOptions().getOutputPath());
        workingDirectory.setText(s.getOptions().getWorkingDirectory());
        programArguments.setText(s.getOptions().getProgramArguments());
    }

    @Override
    protected void applyEditorTo(@NotNull OdinTestRunConfiguration s) {
        Object selectedItem = testKindComboBox.getSelectedItem();
        if (selectedItem != null) {
            s.getOptions().setTestKind(selectedItem.toString());
        }
        s.getOptions().setTestFilePath(fileField.getTextField().getText());
        s.getOptions().setCompilerOptions(compilerOptions.getText());
        s.getOptions().setTestNames(testNames.getText());
        s.getOptions().setPackageDirectoryPath(packageDirectoryPath.getText());
        s.getOptions().setOutputPath(outputPath.getText());
        s.getOptions().setWorkingDirectory(workingDirectory.getText());
        s.getOptions().setProgramArguments(programArguments.getText());

    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }

    @Override
    public void checkEditorData(OdinTestRunConfiguration s) {

    }
}
