package com.lasagnerd.odin.sdkConfig;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TitledSeparator;
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
import java.util.Objects;
import java.util.function.Predicate;

public class OdinSdkConfigurable implements Configurable {

    private final Project project;
    private OdinSdkSettingsComponent sdkSettingsComponent;

    public OdinSdkConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Odin";
    }

    @Override
    public @Nullable JComponent createComponent() {
        OdinDebuggerToolchain[] extensions = OdinDebuggerToolchain.DEBUGGER_TOOLCHAIN.getExtensions();
        OdinSdkSettingsComponent odinSdkSettingsComponent = new OdinSdkSettingsComponent(extensions);
        this.sdkSettingsComponent = odinSdkSettingsComponent;
        return odinSdkSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        OdinSdkConfigPersistentState state = OdinSdkConfigPersistentState.getInstance(project);
        boolean sameSdkPath = sdkSettingsComponent.getSdkPath()
                .equals(state.sdkPath);

        boolean sameBuildFlags = sdkSettingsComponent.getBuildFlags()
                .equals(state.extraBuildFlags);

        boolean sameSemanticAnnotatorEnabled = sdkSettingsComponent.isSemanticAnnotatorEnabled()
                == state.isSemanticAnnotatorEnabled();

        boolean debuggerIdModified = !sdkSettingsComponent.getDebuggerId().equals(state.getDebuggerId());
        boolean debuggerPathModified = !sdkSettingsComponent.getDebuggerPath().equals(state.getDebuggerPath());
        boolean odinCheckerEnabledModified = sdkSettingsComponent.isOdinCheckerEnabled() != state.isOdinCheckerEnabled();
        return !sameSdkPath
                || !sameBuildFlags
                || !sameSemanticAnnotatorEnabled
                || debuggerIdModified
                || debuggerPathModified
                || odinCheckerEnabledModified;
    }

    @Override
    public void apply() {
        OdinSdkConfigPersistentState config = OdinSdkConfigPersistentState.getInstance(project);
        String sdkPath = sdkSettingsComponent.getSdkPath();
        config.setSdkPath(sdkPath);
        config.setExtraBuildFlags(sdkSettingsComponent.getBuildFlags());
        config.setSemanticAnnotatorEnabled(sdkSettingsComponent.isSemanticAnnotatorEnabled() ? "true" : "false");
        config.setDebuggerId(sdkSettingsComponent.getDebuggerId());
        config.setDebuggerPath(sdkSettingsComponent.getDebuggerPath());
        config.setOdinCheckerEnabled(sdkSettingsComponent.isOdinCheckerEnabled() ? "true" : "false");

        OdinSdkLibraryManager.addOrUpdateOdinSdkLibrary(project, sdkPath);
    }

    @Override
    public void reset() {
        OdinSdkConfigPersistentState state = OdinSdkConfigPersistentState.getInstance(project);
        String sdkPath = state.sdkPath;
        String extraBuildFlags = state.extraBuildFlags;
        sdkSettingsComponent.setSdkPath(sdkPath);
        sdkSettingsComponent.setBuildFlags(extraBuildFlags);
        sdkSettingsComponent.setSemanticAnnotatorEnabled(state.isSemanticAnnotatorEnabled());
        sdkSettingsComponent.setDebuggerPath(state.getDebuggerPath());
        sdkSettingsComponent.setDebuggerId(state.getDebuggerId());
        sdkSettingsComponent.setOdinCheckerEnabled(state.isOdinCheckerEnabled());
    }

    @Override
    public void disposeUIResources() {
        this.sdkSettingsComponent = null;
    }

    public class OdinSdkSettingsComponent implements Disposable {

        private final JPanel mainPanel;
        private final OdinDebuggerToolchain[] extensions;
        private TextFieldWithBrowseButton sdkPathTextField;
        private JBTextField buildFlagsTextField;
        private JBCheckBox semanticAnnotatorEnabled;
        private JBCheckBox odinCheckerCheckbox;
        @Nullable
        private ComboBox<Item> debuggerCombobox;
        @Nullable
        private TextFieldWithBrowseButton debuggerPathField;
        @Nullable
        private JButton downloadButton;

        @Override
        public void dispose() {

        }

        public boolean isSemanticAnnotatorEnabled() {
            return this.semanticAnnotatorEnabled.isSelected();
        }

        public void setOdinCheckerEnabled(boolean enabled) {
            odinCheckerCheckbox.setSelected(enabled);
        }

        public boolean isOdinCheckerEnabled() {
            return odinCheckerCheckbox.isSelected();
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

        public OdinSdkSettingsComponent(OdinDebuggerToolchain[] extensions) {
            this.extensions = extensions;
            FormBuilder formBuilder = FormBuilder.createFormBuilder()
                    .addLabeledComponent(
                            new JBLabel("Path to SDK: "),
                            createSdkPathTextFieldWithBrowseButton(), 1, false)
                    .addVerticalGap(10)
                    .addLabeledComponent(
                            new JBLabel("Extra build flags: "),
                            createBuildFlagsTextField(), 1, false)
                    .addComponentToRightColumn(createComment(
                            "Optional. Space separated build flags passed to 'odin check'.<br><br>" +
                                    "Useful flags:<ul>" +
                                    "<li>-vet -vet-cast -strict-style (for more checks)</li>" +
                                    "<li>-max-error-count:999 (to report more errors)</li>" +
                                    "</ul>"), 0);
            if (extensions.length > 0) {
                formBuilder
                        .addComponent(new TitledSeparator("Debugger"))
                        .addLabeledComponent(new JBLabel("Debugger"), createDebuggerComboBox())
                        .addLabeledComponent(new JBLabel("Debugger path"), createDebuggerPathField())
                        .addComponentToRightColumn(createDownloadButton());
            }
            mainPanel = formBuilder
                    .addComponent(new TitledSeparator("Miscellaneous"))
                    .addLabeledComponent(new JBLabel("Semantic annotator"), createCustomAnnotatorCheckbox(), 1)
                    .addComponentToRightColumn(createComment(
                            """
                            <html><p>Turn this off if you get StackOverflowError's or when the <br>'Analyzing...' message on the top right corner won't disappear</p></html>
                            """.stripIndent()), 0)
                    .addLabeledComponent(new JBLabel("Odin checker"), createOdinCheckerCheckbox(), 1)
                    .addComponentToRightColumn(createComment("Enable/Disable the odin checker and the respective error messages"))
                    .addComponentFillVertically(new JPanel(), 1)
                    .getPanel();
        }

        private @NotNull JButton createDownloadButton() {
            downloadButton = new JButton(new DownloadDebuggerAction());
            return downloadButton;
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
                if (extension.isAvailable()) {
                    comboBox.addItem(new Item(extension.getId(), extension.getLabel()));
                }
            }
            comboBox.addItemListener(e -> {
                updateDownloadButton();
                updateDebuggerPath();
            });
            this.debuggerCombobox = comboBox;

            return comboBox;
        }


        private void updateDownloadButton() {
            if(downloadButton == null)
                return;
            Action action = downloadButton.getAction();
            if (action instanceof DownloadDebuggerAction downloadDebuggerAction) {
                action.setEnabled(downloadDebuggerAction.enabledCondition());
            }
        }

        private @NotNull JBCheckBox createCustomAnnotatorCheckbox() {
            this.semanticAnnotatorEnabled = new JBCheckBox();
            return semanticAnnotatorEnabled;
        }

        private @NotNull JBCheckBox createOdinCheckerCheckbox() {
            this.odinCheckerCheckbox = new JBCheckBox();
            return odinCheckerCheckbox;
        }

        private JComponent createComment(final @NlsContexts.Label String text) {
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
            if(debuggerCombobox == null)
                return "";
            Object selectedItem = debuggerCombobox.getSelectedItem();
            if (selectedItem instanceof Item item) {
                return item.id;
            }
            return "";
        }

        public String getDebuggerPath() {
            if(debuggerPathField == null)
                return "";
            return debuggerPathField.getText();
        }

        public void setSdkPath(@NotNull String newText) {
            sdkPathTextField.setText(newText);
        }

        public void setBuildFlags(@NotNull String newBuildFlags) {
            buildFlagsTextField.setText(newBuildFlags);
        }

        public void setDebuggerId(String debuggerId) {
            if(debuggerCombobox == null)
                return;
            OdinDebuggerToolchain toolchainProvider = getToolchain(debuggerId);
            if (toolchainProvider != null) {
                debuggerCombobox.setSelectedItem(
                        new Item(debuggerId, toolchainProvider.getLabel())
                );
            }

            updateDownloadButton();
        }

        public void setDebuggerPath(String debuggerPath) {
            if(debuggerPathField == null)
                return;
            updateDebuggerPath();
            debuggerPathField.setText(debuggerPath);
        }

        private void updateDebuggerPath() {
            if(debuggerPathField == null)
                return;

            JBTextField textField = (JBTextField) debuggerPathField.getTextField();
            OdinDebuggerToolchain selectedToolchain = getSelectedToolchain();
            if (selectedToolchain != null) {
                if (selectedToolchain.isBundled()) {
                    debuggerPathField.setEditable(false);
                    debuggerPathField.setEnabled(false);
                } else {
                    debuggerPathField.setEditable(true);
                    debuggerPathField.setEnabled(true);
                    if (selectedToolchain.isAvailable()) {
                        String detected = selectedToolchain.detect();
                        textField.getEmptyText().setText(Objects.requireNonNullElse(detected, ""));
                    } else {
                        textField.getEmptyText().setText("");
                    }
                }
            } else {
                debuggerPathField.setEditable(true);
                debuggerPathField.setEnabled(true);
                textField.getEmptyText().setText("");
            }
        }

        public void setSemanticAnnotatorEnabled(boolean enabled) {
            semanticAnnotatorEnabled.setSelected(enabled);
        }

        private OdinDebuggerToolchain getSelectedToolchain() {
            if (debuggerCombobox == null)
                return null;
            Object selectedItem = debuggerCombobox.getSelectedItem();
            if (selectedItem instanceof Item item) {
                return getToolchain(item.id());
            }
            return null;
        }

        private OdinDebuggerToolchain getToolchain(String id) {
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

                if(debuggerCombobox == null || debuggerPathField == null)
                    return;
                Item selectedItem = (Item) debuggerCombobox.getSelectedItem();
                if (selectedItem == null) {
                    return;
                }

                OdinDebuggerToolchain toolchainProvider = getToolchain(selectedItem.id);
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

        private class DownloadDebuggerAction extends AbstractAction {

            public DownloadDebuggerAction() {
                super("Download");
            }

            boolean enabledCondition() {
                OdinDebuggerToolchain selectedToolchain = getSelectedToolchain();
                return selectedToolchain != null && selectedToolchain.isDownloadable();
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if(debuggerPathField == null)
                    return;
                OdinDebuggerToolchain selectedToolchain = getSelectedToolchain();
                if (selectedToolchain != null) {
                    if (selectedToolchain.isDownloadable()) {
                        String debuggerExecutablePath = selectedToolchain.download(project);
                        if (debuggerExecutablePath != null) {
                            debuggerPathField.setText(debuggerExecutablePath);
                        }
                    }
                }
            }
        }
    }
}
