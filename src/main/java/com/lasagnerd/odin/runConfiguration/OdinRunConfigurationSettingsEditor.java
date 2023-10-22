package com.lasagnerd.odin.runConfiguration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinRunConfigurationSettingsEditor extends SettingsEditor<OdinRunConfiguration> {

    private final JPanel panel;
    private final JTextField compilerOptions;
    private final JTextField workingDirectory;

    public OdinRunConfigurationSettingsEditor() {
        // Get project
        compilerOptions = new JTextField("run .");
        workingDirectory = new JTextField("$ProjectFileDir$");
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JLabel("Compiler Options"), compilerOptions)
                .addLabeledComponent(new JLabel("Working Directory"), workingDirectory)
                .getPanel();
    }

    @Override
    protected void resetEditorFrom(@NotNull OdinRunConfiguration s) {
        compilerOptions.setText(s.getOptions().getCompilerOptions());
        workingDirectory.setText(s.getOptions().getFilePath());
    }

    @Override
    protected void applyEditorTo(@NotNull OdinRunConfiguration s) throws ConfigurationException {
        s.getOptions().setCompilerOptions(compilerOptions.getText());
        s.getOptions().setFilePath(workingDirectory.getText());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }
}
