package com.lasagnerd.odin.runConfiguration;

import com.intellij.execution.impl.CheckableRunConfigurationEditor;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinRunConfigurationSettingsEditor extends SettingsEditor<OdinRunConfiguration> implements CheckableRunConfigurationEditor<OdinRunConfiguration> {

    private final JPanel panel;
    private final JTextField compilerOptions;
    private final JTextField projectProjectDirectoryPath;
    private final JTextField outputDirectory;

    public OdinRunConfigurationSettingsEditor() {
        // Get project
        compilerOptions = new JTextField("run .");
        projectProjectDirectoryPath = new JTextField("$ProjectFileDir$");
        outputDirectory = new JTextField("$ProjectFileDir$/bin");

        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JLabel("Output Directory"), outputDirectory)
                .addLabeledComponent(new JLabel("Compiler Options"), compilerOptions)
                .addLabeledComponent(new JLabel("Project Directory"), projectProjectDirectoryPath)
                .getPanel();
    }

    @Override
    protected void resetEditorFrom(@NotNull OdinRunConfiguration s) {
        compilerOptions.setText(s.getOptions().getCompilerOptions());
        projectProjectDirectoryPath.setText(s.getOptions().getProjectDirectoryPath());
        outputDirectory.setText(s.getOptions().getOutputPath());
    }

    @Override
    protected void applyEditorTo(@NotNull OdinRunConfiguration s) {
        s.getOptions().setCompilerOptions(compilerOptions.getText());
        s.getOptions().setProjectDirectoryPath(projectProjectDirectoryPath.getText());
        s.getOptions().setOutputPath(outputDirectory.getText());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }

    @Override
    public void checkEditorData(OdinRunConfiguration s) {

    }
}
