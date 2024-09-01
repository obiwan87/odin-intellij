package com.lasagnerd.odin.sdkConfig;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.function.Predicate;

public class OdinSdkSettingsComponent implements Disposable {

    private final JPanel mainPanel;
    private final OdinDebuggerToolchain[] extensions;
    private final Project project;
    private TextFieldWithBrowseButton sdkPathTextField;
    private JBTextField buildFlagsTextField;
    private JBCheckBox semanticAnnotatorEnabled;
    private ComboBox<Item> debuggerCombobox;
    private TextFieldWithBrowseButton debuggerPathField;

    @Override
    public void dispose() {

    }

    public boolean isSemanticAnnotatorEnabled() {
        return this.semanticAnnotatorEnabled.isSelected();
    }

    public void setDebuggerId(String debuggerId) {
        OdinDebuggerToolchain toolchainProvider = getProvider(debuggerId);
        if (toolchainProvider != null) {
            debuggerCombobox.setSelectedItem(
                    new Item(debuggerId, toolchainProvider.getLabel())
            );
        }
    }

    public void setDebuggerPath(String debuggerPath) {
        debuggerPathField.setText(debuggerPath);
    }

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
            String path = virtualFile.getPath();
            setSdkPath(path);
        }
    }

    public OdinSdkSettingsComponent(OdinDebuggerToolchain[] extensions, Project project) {
        this.extensions = extensions;
        this.project = project;
        FormBuilder formBuilder = FormBuilder.createFormBuilder()
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
                                "<li>-vet -vet-cast -strict-style (for more checks)</li>" +
                                "<li>-max-error-count:999 (to report more errors)</li>" +
                                "</ul>"), 0);

        if (extensions.length > 0) {
            formBuilder
                    .addLabeledComponent(new JBLabel("Debugger"), createDebuggerComboBox())
                    .addLabeledComponent(new JBLabel("Debugger path"), createDebuggerPathField());
        }
        mainPanel = formBuilder
                .addLabeledComponent(new JBLabel("Semantic annotator"), createCustomAnnotatorCheckbox(), 1)
                .addComponentFillVertically(new JPanel(), 1)
                .getPanel();
    }

    private @NotNull JComponent createDebuggerPathField() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = createGridBagConstraintsForFirstColumn();

        debuggerPathField = new TextFieldWithBrowseButton(new ExtendableTextField(10), new DebugExecutableSelector());


        panel.add(debuggerPathField, constraints);
        return panel;
    }

    private static @NotNull Predicate<String> exeMatcher(String fileName) {
        return e1 -> e1.equals(fileName) || (e1 + ".exe").equals(fileName);
    }

    record Item(String id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private @NotNull ComboBox<?> createDebuggerComboBox() {
        ComboBox<Item> comboBox = new ComboBox<>();

        for (OdinDebuggerToolchain extension : extensions) {
            comboBox.addItem(new Item(extension.getId(), extension.getLabel()));
        }
        this.debuggerCombobox = comboBox;
        return comboBox;
    }

    private @NotNull JBCheckBox createCustomAnnotatorCheckbox() {
        this.semanticAnnotatorEnabled = new JBCheckBox();
        return semanticAnnotatorEnabled;
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
        sdkPathTextField = new TextFieldWithBrowseButton(new ExtendableTextField(10), new BrowseToSdkFileChooserAction());
        panel.add(sdkPathTextField, constraints);

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

    @NotNull
    public String getDebuggerId() {
        Object selectedItem = debuggerCombobox.getSelectedItem();
        if (selectedItem instanceof Item item) {
            return item.id;
        }
        return "";
    }

    public String getDebuggerPath() {
        return debuggerPathField.getText();
    }

    public void setSdkPath(@NotNull String newText) {
        sdkPathTextField.setText(newText);
    }


    public void setBuildFlags(@NotNull String newBuildFlags) {
        buildFlagsTextField.setText(newBuildFlags);
    }

    public void setSemanticAnnotatorEnabled(boolean enabled) {
        semanticAnnotatorEnabled.setSelected(enabled);
    }

    private OdinDebuggerToolchain getProvider(String id) {
        for (OdinDebuggerToolchain extension : extensions) {
            if (extension.getId().equals(id)) {
                return extension;
            }
        }

        return null;
    }

    private class DebugExecutableSelector extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Item selectedItem = (Item) debuggerCombobox.getSelectedItem();
            if (selectedItem == null) {
                return;
            }

            OdinDebuggerToolchain toolchainProvider = getProvider(selectedItem.id);
            if (toolchainProvider == null)
                return;

            FileChooserDescriptor descriptor = new FileChooserDescriptor(
                    true,
                    false,
                    false,
                    false,
                    false,
                    false
            ) {
                @Override
                public boolean isFileSelectable(@Nullable VirtualFile file) {

                    boolean fileSelectable = super.isFileSelectable(file);
                    if (fileSelectable) {
                        String fileName = file.getName();
                        return Arrays.stream(toolchainProvider.getExecutableNames())
                                .anyMatch(exeMatcher(fileName));
                    }
                    return false;
                }

                @Override
                public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                    String fileName = file.getName();

                    return file.isDirectory() || Arrays.stream(toolchainProvider.getExecutableNames())
                            .anyMatch(exeMatcher(fileName));
                }
            };


            VirtualFile virtualFile = FileChooser.chooseFile(
                    descriptor,
                    null,
                    null
            );
            if (virtualFile == null) {
                return;
            }
            String path = virtualFile.getPath();
            OdinSdkSettingsComponent.this.debuggerPathField.setText(path);
        }
    }
}