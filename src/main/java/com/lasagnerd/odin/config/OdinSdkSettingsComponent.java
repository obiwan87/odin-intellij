package com.lasagnerd.odin.config;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBOptionButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class OdinSdkSettingsComponent {
    class OpenFileChooserAction extends AbstractAction {
        public OpenFileChooserAction() {
            super("Browse");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            VirtualFile virtualFile = FileChooser.chooseFile(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    null,
                    null
            );
            if (virtualFile == null) return;
            setSdkPath(virtualFile.getPath());
        }
    }

    private final JPanel mainPanel;
    private JBTextField sdkPathText;


    public OdinSdkSettingsComponent() {


        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("SDK path: "), createTextFieldWithBrowseButton(), 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private JComponent createTextFieldWithBrowseButton() {
        JPanel panel = new JPanel(new GridBagLayout());
        // Create GridBagConstraints
        GridBagConstraints constraints = new GridBagConstraints();

        // Create the text field
        sdkPathText = new JBTextField(20);

        // Add the text field to the frame
        constraints.gridx = 0;  // column 0
        constraints.gridy = 0;  // row 0
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;  // allow horizontal expansion
        panel.add(sdkPathText, constraints);

        // Create the browse button
        JBOptionButton browseButton = new JBOptionButton(new OpenFileChooserAction(), null);

        // Add the browse button to the frame
        constraints.gridx = 1;  // column 1
        constraints.gridy = 0;  // row 0
        constraints.fill = GridBagConstraints.NONE;  // reset to default
        constraints.weightx = 0;  // no expansion
        constraints.insets = JBUI.insetsLeft(5);  // padding
        panel.add(browseButton, constraints);

        return panel;
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    @NotNull
    public String getSdkPath() {
        return sdkPathText.getText();
    }

    public void setSdkPath(@NotNull String newText) {
        sdkPathText.setText(newText);
    }
}
