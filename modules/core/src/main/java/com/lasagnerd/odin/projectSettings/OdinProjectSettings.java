package com.lasagnerd.odin.projectSettings;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SimpleColoredComponent;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OdinProjectSettings implements Disposable {
    private JComponent component;
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
    private JBCheckBox highlightUnknownReferencesEnabledCheckbox;
    private SimpleColoredComponent sdkVersion;

    private final Object MUTEX = new Object();
    private boolean diposed;

    @Override
    public void dispose() {
        synchronized (MUTEX) {

            // Dispose of UI components
            if (component != null) {
                component = null;
            }
            if (sdkPathTextField != null) {
                Document document = sdkPathTextField.getTextField().getDocument();
                if (document instanceof AbstractDocument abstractDocument) {
                    for (DocumentListener documentListener : abstractDocument.getDocumentListeners()) {
                        document.removeDocumentListener(documentListener);
                    }
                }
                sdkPathTextField.dispose();
                sdkPathTextField = null;
            }
            if (buildFlagsTextField != null) {
                buildFlagsTextField = null; // JBTextField does not require explicit dispose
            }
            if (semanticAnnotatorEnabled != null) {
                semanticAnnotatorEnabled = null;
            }
            if (odinCheckerCheckbox != null) {
                odinCheckerCheckbox = null;
            }
            if (debuggerCombobox != null) {
                debuggerCombobox = null;
            }
            if (debuggerPathField != null) {
                debuggerPathField.dispose();
                debuggerPathField = null;
            }
            if (downloadButton != null) {
                for (ActionListener listener : downloadButton.getActionListeners()) {
                    downloadButton.removeActionListener(listener);
                }
                downloadButton = null;
            }
            if (highlightUnknownReferencesEnabledCheckbox != null) {
                highlightUnknownReferencesEnabledCheckbox = null;
            }
            if (sdkVersion != null) {
                sdkVersion.clear(); // Clear any text or icons
                sdkVersion = null;
            }
            diposed = true;
        }
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

    public boolean isHighlightUnknownReferencesEnabled() {
        return highlightUnknownReferencesEnabledCheckbox.isSelected();
    }

    public void setHighlightUnknownReferences(boolean highlightUnknownReferencesEnabled) {
        this.highlightUnknownReferencesEnabledCheckbox.setSelected(highlightUnknownReferencesEnabled);
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

    public OdinProjectSettings() {
        this.extensions = OdinDebuggerToolchain.DEBUGGER_TOOLCHAIN.getExtensions();

    }

    private @NotNull JComponent createHighlightUnknownReferencesCheckbox() {
        highlightUnknownReferencesEnabledCheckbox = new JBCheckBox();
        return highlightUnknownReferencesEnabledCheckbox;
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
        if (downloadButton == null)
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

        // Components initialization
        ComponentValidator componentValidator = new ComponentValidator(this)
                .withValidator(sdkPathValidator())
                .andStartOnFocusLost()
                .andRegisterOnDocumentListener(sdkPathTextField.getTextField())
                .installOn(sdkPathTextField.getTextField());

        sdkPathTextField.getTextField().getDocument().addDocumentListener(new VersionListener());

        return panel;
    }

    private @NotNull Supplier<ValidationInfo> sdkPathValidator() {
        return () -> {
            if (sdkPathTextField != null) {
                String sdkPath = sdkPathTextField.getText();
                return validateSdkPath(sdkPath);
            }
            return null;
        };
    }

    private @Nullable ValidationInfo validateSdkPath(String sdkPath) {
        if (StringUtil.isNotEmpty(sdkPath)) {
            Path nioPath = Path.of(sdkPath);
            if (!nioPath.toFile().exists()) {
                return new ValidationInfo("Path does not exist", sdkPathTextField.getTextField());
            }

            String odinBinaryPath = OdinSdkUtils.getOdinBinaryPath(nioPath.toString());
            File binaryFile = new File(odinBinaryPath);
            if (!binaryFile.exists() || !binaryFile.isFile()) {
                return new ValidationInfo("Odin binary not found", sdkPathTextField.getTextField());
            }
        }
        return null;
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

    public JComponent getComponent() {
        synchronized (MUTEX) {
            if (component != null)
                return component;

            if(diposed)
                return null;

            sdkVersion = new SimpleColoredComponent();

            FormBuilder formBuilder = FormBuilder.createFormBuilder()
                    .addLabeledComponent(
                            new JBLabel("Path to SDK: "),
                            createSdkPathTextFieldWithBrowseButton(), 1, false)
                    .addComponentToRightColumn(sdkVersion)
                    .addVerticalGap(10)
                    .addLabeledComponent(
                            new JBLabel("Checker arguments: "),
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

            component = formBuilder
                    .addComponent(new TitledSeparator("Miscellaneous"))
                    .addLabeledComponent(new JBLabel("Semantic annotator"), createCustomAnnotatorCheckbox(), 1)
                    .addComponentToRightColumn(createComment(
                            """
                                    <html><p>Turn this off if you get StackOverflowError's or when the <br>'Analyzing...' message on the top right corner won't disappear</p></html>
                                    """.stripIndent()), 0)
                    .addLabeledComponent(new JBLabel("Odin checker"), createOdinCheckerCheckbox(), 1)
                    .addComponentToRightColumn(createComment("Enable/Disable the odin checker and the respective error messages"))
                    .addLabeledComponent(new JBLabel("Highlight unknown references (experimental)"), createHighlightUnknownReferencesCheckbox(), 1)
                    .addComponentToRightColumn(createComment("Enable/Disable highlighting of unknown references"))
                    .addComponentFillVertically(new JPanel(), 1)
                    .getPanel();

            return component;
        }
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
        if (debuggerCombobox == null)
            return "";
        Object selectedItem = debuggerCombobox.getSelectedItem();
        if (selectedItem instanceof Item item) {
            return item.id;
        }
        return "";
    }

    public String getDebuggerPath() {
        if (debuggerPathField == null)
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
        if (debuggerCombobox == null)
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
        if (debuggerPathField == null)
            return;
        updateDebuggerPath();
        debuggerPathField.setText(debuggerPath);
    }

    private void updateDebuggerPath() {
        if (debuggerPathField == null)
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

            if (debuggerCombobox == null || debuggerPathField == null)
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
            OdinProjectSettings.this.debuggerPathField.setText(path);
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
            if (debuggerPathField == null)
                return;
            OdinDebuggerToolchain selectedToolchain = getSelectedToolchain();
            if (selectedToolchain != null) {
                if (selectedToolchain.isDownloadable()) {
                    String debuggerExecutablePath = selectedToolchain.download();
                    if (debuggerExecutablePath != null) {
                        debuggerPathField.setText(debuggerExecutablePath);
                    }
                }
            }
        }
    }

    private class VersionListener extends DocumentAdapter {
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
            if (validateSdkPath(sdkPathTextField.getText()) == null) {
                sdkVersion.clear();
                if (sdkPathTextField.getText().isBlank())
                    return;

                sdkVersion.setIcon(AnimatedIcon.Default.INSTANCE);
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    String odinSdkVersion = OdinSdkUtils.getOdinSdkVersion(sdkPathTextField.getText());
                    sdkVersion.clear();
                    sdkVersion.setIcon(AllIcons.General.GreenCheckmark);
                    sdkVersion.append("Version: " + odinSdkVersion);
                });

            } else {
                sdkVersion.clear();
                sdkVersion.setIcon(AllIcons.General.Error);
                sdkVersion.append("Invalid path");
            }
        }
    }
}
