package com.lasagnerd.odin.sdkConfig;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBOptionButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class OdinSdkSettingsComponent {

    private final JPanel mainPanel;
    private JBTextField sdkPathTextField;
    private JBTextField buildFlagsTextField;

    class BrowseToSdkFileChooserAction extends AbstractAction {
        public BrowseToSdkFileChooserAction() {
            super("Browse");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            VirtualFile virtualFile = FileChooser.chooseFile(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    null,
                    null
            );
            if (virtualFile == null) {
                return;
            }
            setSdkPath(virtualFile.getPath());
        }
    }

    public OdinSdkSettingsComponent() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(
                        new JBLabel("Path to SDK: "),
                        createSdkPathTextFieldWithBrowseButton(), 1, false)
                .addVerticalGap(10)
                .addLabeledComponent(
                        new JBLabel("Extra build flags: "),
                        createBuildFlagsTextField(), 1, false)
                .addComponentToRightColumn(createLabel(
                        "Optional. Space separated build flags passed to 'odin check'.<br><br>" +
                                "Useful flags:<ul>" +
                                "<li>-o:none (for fastest compile times)</li>" +
                                "<li>-vet -vet-cast -strict-style (for more checks)</li>" +
                                "<li>-max-error-count:999 (to report more errors)</li>" +
                                "</ul>"), 0)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @SuppressWarnings("SameParameterValue")
    private JComponent createLabel(final @NlsContexts.Label String text) {
        final JBLabel label = new JBLabel(text, UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER);
        label.setBorder(JBUI.Borders.emptyLeft(3));
        label.setCopyable(true);
        label.setAllowAutoWrapping(true);
        return label;
    }

    private JComponent createSdkPathTextFieldWithBrowseButton() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = createGridBagConstraintsForFirstColumn();
        sdkPathTextField = new JBTextField(20);
        panel.add(sdkPathTextField, constraints);

        JBOptionButton browseButton = new JBOptionButton(new BrowseToSdkFileChooserAction(), null);
        panel.add(browseButton, createGridBagConstraintsForSecondColumn());

        return panel;
    }

    private JComponent createBuildFlagsTextField() {
        JPanel panel = new JPanel(new GridBagLayout());
        buildFlagsTextField = new JBTextField(20);
        panel.add(buildFlagsTextField, createGridBagConstraintsForFirstColumn());
        return panel;
    }

    private GridBagConstraints createGridBagConstraintsForFirstColumn() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;  // column 0
        constraints.gridy = 0;  // row 0
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;  // allow horizontal expansion
        return constraints;
    }

    private GridBagConstraints createGridBagConstraintsForSecondColumn() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;  // column 1
        constraints.gridy = 0;  // row 0
        constraints.fill = GridBagConstraints.NONE;  // reset to default
        constraints.weightx = 0;  // no expansion
        constraints.insets = JBUI.insetsLeft(5);  // padding
        return constraints;
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    @NotNull
    public String getSdkPath() {
        return sdkPathTextField.getText();
    }

    @NotNull
    public String getBuildFlags() {
        return buildFlagsTextField.getText();
    }

    public void setSdkPath(@NotNull String newText) {
        sdkPathTextField.setText(newText);
    }


    public void setBuildFlags(@NotNull String newBuildFlags) {
        buildFlagsTextField.setText(newBuildFlags);
    }

}