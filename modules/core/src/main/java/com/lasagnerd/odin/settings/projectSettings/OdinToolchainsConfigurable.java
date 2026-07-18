package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class OdinToolchainsConfigurable implements Configurable {
    private final List<OdinToolchainState> toolchains = new ArrayList<>();
    private CollectionListModel<OdinToolchainState> listModel;
    private JBList<OdinToolchainState> toolchainList;
    private JBTextField nameField;
    private TextFieldWithBrowseButton compilerField;
    private TextFieldWithBrowseButton librariesField;
    private ComboBox<DebuggerItem> debuggerCombo;
    private TextFieldWithBrowseButton debuggerPathField;
    private JButton downloadDebuggerButton;
    private JPanel component;
    private int selectedIndex = -1;
    private boolean updating;

    private record DebuggerItem(String id, String label) {
        @Override public String toString() { return label; }
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Odin Toolchains";
    }

    @Override
    public @Nullable JComponent createComponent() {
        listModel = new CollectionListModel<>();
        toolchainList = new JBList<>(listModel);
        toolchainList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolchainList.setCellRenderer((list, value, index, selected, focused) ->
                new DefaultListCellRenderer().getListCellRendererComponent(
                        list, Objects.requireNonNullElse(value.name, "Unnamed toolchain"), index, selected, focused));
        toolchainList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !updating) select(toolchainList.getSelectedIndex());
        });

        JButton add = new JButton("Add");
        add.addActionListener(event -> addToolchain());
        JButton remove = new JButton("Remove");
        remove.addActionListener(event -> removeToolchain());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.add(add);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(remove);

        JPanel left = new JPanel(new BorderLayout(0, 5));
        left.add(new JScrollPane(toolchainList), BorderLayout.CENTER);
        left.add(buttons, BorderLayout.SOUTH);
        left.setPreferredSize(new Dimension(220, 300));

        nameField = new JBTextField();
        compilerField = new TextFieldWithBrowseButton();
        compilerField.addActionListener(event -> chooseCompiler());
        librariesField = new TextFieldWithBrowseButton();
        librariesField.addActionListener(event -> chooseLibraries());
        debuggerCombo = new ComboBox<>();
        debuggerCombo.addItem(new DebuggerItem("", "None"));
        for (OdinDebuggerToolchain debugger : OdinDebuggerToolchain.DEBUGGER_TOOLCHAIN.getExtensions()) {
            if (debugger.isAvailable()) debuggerCombo.addItem(new DebuggerItem(debugger.getId(), debugger.getLabel()));
        }
        debuggerCombo.addItemListener(event -> {
            if (!updating && event.getStateChange() == java.awt.event.ItemEvent.SELECTED) updateDebuggerControls();
        });
        debuggerPathField = new TextFieldWithBrowseButton();
        debuggerPathField.addActionListener(event -> chooseDebugger());
        downloadDebuggerButton = new JButton("Download");
        downloadDebuggerButton.addActionListener(event -> downloadDebugger());
        JPanel debuggerPathPanel = new JPanel(new BorderLayout(5, 0));
        debuggerPathPanel.add(debuggerPathField, BorderLayout.CENTER);
        debuggerPathPanel.add(downloadDebuggerButton, BorderLayout.EAST);

        JComponent editor = FormBuilder.createFormBuilder()
                .addLabeledComponent("Name:", nameField)
                .addLabeledComponent("Odin executable:", compilerField)
                .addLabeledComponent("Odin libraries:", librariesField)
                .addLabeledComponent("Debugger:", debuggerCombo)
                .addLabeledComponent("Debugger path:", debuggerPathPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        component = new JPanel(new BorderLayout(15, 0));
        component.add(left, BorderLayout.WEST);
        component.add(editor, BorderLayout.CENTER);
        loadFromService();
        return component;
    }

    private void select(int index) {
        flushSelected();
        selectedIndex = index;
        OdinToolchainState selected = index >= 0 && index < toolchains.size() ? toolchains.get(index) : null;
        updating = true;
        nameField.setText(selected == null ? "" : value(selected.name));
        compilerField.setText(selected == null ? "" : value(selected.compilerPath));
        librariesField.setText(selected == null ? "" : value(selected.libraryPath));
        debuggerPathField.setText(selected == null ? "" : value(selected.debuggerPath));
        selectDebugger(selected == null ? "" : selected.debuggerId);
        setEditorEnabled(selected != null);
        updating = false;
        updateDebuggerControls();
    }

    private void flushSelected() {
        if (updating || selectedIndex < 0 || selectedIndex >= toolchains.size()) return;
        OdinToolchainState selected = toolchains.get(selectedIndex);
        selected.name = nameField.getText();
        selected.compilerPath = compilerField.getText();
        selected.libraryPath = librariesField.getText();
        selected.debuggerPath = debuggerPathField.getText();
        DebuggerItem debugger = (DebuggerItem) debuggerCombo.getSelectedItem();
        selected.debuggerId = debugger == null ? "" : debugger.id;
        listModel.contentsChanged(selected);
    }

    private void addToolchain() {
        flushSelected();
        OdinToolchainState toolchain = new OdinToolchainState();
        toolchain.id = UUID.randomUUID().toString();
        toolchain.name = uniqueName();
        toolchains.add(toolchain);
        listModel.add(toolchain);
        toolchainList.setSelectedIndex(toolchains.size() - 1);
        nameField.requestFocusInWindow();
    }

    private void removeToolchain() {
        int index = toolchainList.getSelectedIndex();
        if (index < 0) return;
        selectedIndex = -1;
        toolchains.remove(index);
        listModel.remove(index);
        if (!toolchains.isEmpty()) toolchainList.setSelectedIndex(Math.min(index, toolchains.size() - 1));
        else select(-1);
    }

    private String uniqueName() {
        String base = "Odin Toolchain";
        String candidate = base;
        int suffix = 2;
        while (containsName(candidate)) candidate = base + " (" + suffix++ + ")";
        return candidate;
    }

    private boolean containsName(String name) {
        return toolchains.stream().anyMatch(it -> name.equals(it.name));
    }

    private void setEditorEnabled(boolean enabled) {
        nameField.setEnabled(enabled);
        compilerField.setEnabled(enabled);
        librariesField.setEnabled(enabled);
        debuggerCombo.setEnabled(enabled);
        debuggerPathField.setEnabled(enabled);
        downloadDebuggerButton.setEnabled(enabled && selectedDebuggerToolchain() != null
                && selectedDebuggerToolchain().isDownloadable());
    }

    private void selectDebugger(String id) {
        for (int i = 0; i < debuggerCombo.getItemCount(); i++) {
            if (Objects.equals(debuggerCombo.getItemAt(i).id, id)) {
                debuggerCombo.setSelectedIndex(i);
                return;
            }
        }
        debuggerCombo.setSelectedIndex(0);
    }

    private void updateDebuggerControls() {
        OdinDebuggerToolchain debugger = selectedDebuggerToolchain();
        boolean hasSelectedToolchain = selectedIndex >= 0 && selectedIndex < toolchains.size();
        boolean bundled = debugger != null && debugger.isBundled();
        debuggerPathField.setEnabled(hasSelectedToolchain && !bundled);
        debuggerPathField.setEditable(hasSelectedToolchain && !bundled);
        downloadDebuggerButton.setEnabled(hasSelectedToolchain && debugger != null && debugger.isDownloadable());
        JBTextField textField = (JBTextField) debuggerPathField.getTextField();
        String detected = debugger == null ? null : debugger.detect();
        textField.getEmptyText().setText(Objects.requireNonNullElse(detected, ""));
    }

    private OdinDebuggerToolchain selectedDebuggerToolchain() {
        DebuggerItem selected = (DebuggerItem) debuggerCombo.getSelectedItem();
        if (selected == null || selected.id.isBlank()) return null;
        for (OdinDebuggerToolchain debugger : OdinDebuggerToolchain.DEBUGGER_TOOLCHAIN.getExtensions()) {
            if (selected.id.equals(debugger.getId())) return debugger;
        }
        return null;
    }

    private void downloadDebugger() {
        OdinDebuggerToolchain debugger = selectedDebuggerToolchain();
        if (debugger == null || !debugger.isDownloadable()) return;
        String downloadedPath = debugger.download();
        if (downloadedPath != null) debuggerPathField.setText(downloadedPath);
    }

    private void chooseCompiler() {
        VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.singleFile(), null, null);
        if (file != null) compilerField.setText(file.getPath());
    }

    private void chooseLibraries() {
        VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), null, null);
        if (file != null) librariesField.setText(file.getPath());
    }

    private void chooseDebugger() {
        VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.singleFile(), null, null);
        if (file != null) debuggerPathField.setText(file.getPath());
    }

    @Override
    public boolean isModified() {
        flushSelected();
        return !toolchains.equals(OdinToolchainService.getInstance().getToolchains());
    }

    @Override
    public void apply() throws ConfigurationException {
        flushSelected();
        for (OdinToolchainState toolchain : toolchains) {
            if (toolchain.name == null || toolchain.name.isBlank()) throw new ConfigurationException("Toolchain name must not be empty");
            if (toolchain.compilerPath != null && !toolchain.compilerPath.isBlank() && !new File(toolchain.compilerPath).isFile())
                throw new ConfigurationException("Odin executable does not exist: " + toolchain.compilerPath);
            if (toolchain.libraryPath != null && !toolchain.libraryPath.isBlank() && !new File(toolchain.libraryPath).isDirectory())
                throw new ConfigurationException("Odin library directory does not exist: " + toolchain.libraryPath);
        }
        OdinToolchainService.getInstance().replaceToolchains(toolchains.stream().map(OdinToolchainsConfigurable::copy).toList());
    }

    @Override
    public void reset() {
        if (component != null) loadFromService();
    }

    private void loadFromService() {
        updating = true;
        selectedIndex = -1;
        toolchains.clear();
        toolchains.addAll(OdinToolchainService.getInstance().getToolchains().stream().map(OdinToolchainsConfigurable::copy).toList());
        listModel.removeAll();
        listModel.add(toolchains);
        updating = false;
        if (!toolchains.isEmpty()) toolchainList.setSelectedIndex(0);
        else select(-1);
    }

    private static OdinToolchainState copy(OdinToolchainState source) {
        OdinToolchainState copy = new OdinToolchainState();
        copy.id = source.id;
        copy.name = source.name;
        copy.compilerPath = source.compilerPath;
        copy.libraryPath = source.libraryPath;
        copy.debuggerId = source.debuggerId;
        copy.debuggerPath = source.debuggerPath;
        return copy;
    }

    private static String value(String value) { return Objects.requireNonNullElse(value, ""); }

    @Override
    public void disposeUIResources() {
        component = null;
        toolchainList = null;
        listModel = null;
        selectedIndex = -1;
    }
}
