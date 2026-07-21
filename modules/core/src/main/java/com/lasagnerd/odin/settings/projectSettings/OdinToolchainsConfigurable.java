package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.AddDeleteListPanel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.nio.file.Path;
import com.intellij.openapi.util.SystemInfo;

public final class OdinToolchainsConfigurable implements Configurable {
    private final List<OdinToolchainState> items = new ArrayList<>();
    private ToolchainListPanel toolchainListPanel;
    private JBList<OdinToolchainState> list;
    private JBTextField name;
    private ComboBox<RegistryItem> sdk;
    private ComboBox<RegistryItem> debugger;
    private JPanel component;
    private int selected = -1;
    private boolean updating;

    @Override public @NlsContexts.ConfigurableName String getDisplayName() { return "Toolchains"; }
    @Override public @Nullable JComponent createComponent() {
        toolchainListPanel = new ToolchainListPanel();
        list = toolchainListPanel.toolchainList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && !updating) select(list.getSelectedIndex()); });
        name = new JBTextField(); sdk = new ComboBox<>(); debugger = new ComboBox<>();
        reloadRegistryItems();
        JComponent editor = createEditor();
        Splitter splitter = new Splitter(false, 0.3f);
        splitter.setHonorComponentsMinimumSize(false);
        splitter.setFirstComponent(toolchainListPanel); splitter.setSecondComponent(editor);
        component = new JPanel(new BorderLayout()); component.add(splitter);
        load(); return component;
    }
    private JComponent createEditor() {
        JPanel panel = new JPanel(new GridBagLayout());
        addRow(panel, 0, "Name:", name); addRow(panel, 1, "Odin SDK:", sdk); addRow(panel, 2, "Debugger:", debugger);
        GridBagConstraints note = new GridBagConstraints(); note.gridx=0; note.gridy=3; note.gridwidth=2; note.weightx=1; note.fill=GridBagConstraints.HORIZONTAL;
        note.anchor=GridBagConstraints.WEST; note.insets=JBUI.insetsTop(4); panel.add(new JLabel("Changes to this toolchain apply to all projects that use it."), note);
        GridBagConstraints filler = new GridBagConstraints(); filler.gridx=0; filler.gridy=4; filler.gridwidth=2; filler.weightx=1; filler.weighty=1; filler.fill=GridBagConstraints.BOTH;
        panel.add(new JPanel(), filler); panel.setMinimumSize(new Dimension(0, 0)); return panel;
    }
    private static void addRow(JPanel panel, int row, String text, JComponent field) {
        GridBagConstraints label = new GridBagConstraints(); label.gridx=0; label.gridy=row; label.anchor=GridBagConstraints.WEST; label.insets=JBUI.insets(0,0,8,8);
        panel.add(new JLabel(text), label);
        GridBagConstraints value = new GridBagConstraints(); value.gridx=1; value.gridy=row; value.weightx=1; value.fill=GridBagConstraints.HORIZONTAL; value.insets=JBUI.insetsBottom(8);
        field.setMinimumSize(new Dimension(0, field.getPreferredSize().height)); panel.add(field, value);
    }
    private void reloadRegistryItems() {
        sdk.removeAllItems(); sdk.addItem(new RegistryItem("", "None"));
        for (OdinSdkState value : OdinSdkRegistryService.getInstance().getSdks()) sdk.addItem(new RegistryItem(value.id, value.name));
        debugger.removeAllItems(); debugger.addItem(new RegistryItem("", "None"));
        for (OdinDebuggerState value : OdinDebuggerRegistryService.getInstance().getDebuggers()) debugger.addItem(new RegistryItem(value.id, value.name));
    }
    private void select(int index) {
        flush(); selected = index; OdinToolchainState value = index >= 0 && index < items.size() ? items.get(index) : null;
        updating = true; name.setText(value == null ? "" : value.name); choose(sdk, value == null ? "" : value.sdkId);
        choose(debugger, value == null ? "" : value.debuggerConfigId); setEnabled(value != null); updating = false;
    }
    private void choose(ComboBox<RegistryItem> box, String id) {
        for (int i = 0; i < box.getItemCount(); i++) if (Objects.equals(box.getItemAt(i).id, id)) { box.setSelectedIndex(i); return; }
        box.setSelectedIndex(0);
    }
    private void setEnabled(boolean value) { name.setEnabled(value); sdk.setEnabled(value); debugger.setEnabled(value); }
    private void flush() {
        if (updating || selected < 0 || selected >= items.size()) return;
        OdinToolchainState value = items.get(selected); value.name = name.getText();
        value.sdkId = ((RegistryItem) sdk.getSelectedItem()).id; value.debuggerConfigId = ((RegistryItem) debugger.getSelectedItem()).id;
        list.repaint();
    }
    private void add() {
        flush(); OdinToolchainState value = new OdinToolchainState(); value.id = UUID.randomUUID().toString(); value.name = uniqueName();
        items.add(value); toolchainListPanel.addToolchain(value);
    }
    private void remove() {
        int index = list.getSelectedIndex(); if (index < 0) return; selected = -1; items.remove(index); toolchainListPanel.removeAt(index);
        if (items.isEmpty()) select(-1); else list.setSelectedIndex(Math.min(index, items.size() - 1));
    }
    private String uniqueName() { String base = "Odin Toolchain", result = base; int n = 2;
        while (containsName(result)) result = base + " (" + n++ + ")"; return result; }
    private boolean containsName(String candidate) { for (OdinToolchainState item : items) if (candidate.equals(item.name)) return true; return false; }
    @Override public boolean isModified() { flush(); return !items.equals(OdinToolchainService.getInstance().getToolchains()); }
    @Override public void apply() throws ConfigurationException {
        flush(); for (OdinToolchainState value : items) if (value.name == null || value.name.isBlank()) throw new ConfigurationException("Toolchain name must not be empty");
        OdinToolchainService.getInstance().replaceToolchains(items.stream().map(OdinToolchainsConfigurable::copy).toList());
    }
    @Override public void reset() { if (component != null) { reloadRegistryItems(); load(); } }
    private void load() { updating = true; selected = -1; items.clear(); items.addAll(OdinToolchainService.getInstance().getToolchains().stream().map(OdinToolchainsConfigurable::copy).toList());
        toolchainListPanel.setToolchains(items); updating = false; if (items.isEmpty()) select(-1); else list.setSelectedIndex(0); }
    private static OdinToolchainState copy(OdinToolchainState source) { OdinToolchainState value = new OdinToolchainState();
        value.id=source.id; value.name=source.name; value.sdkId=source.sdkId; value.debuggerConfigId=source.debuggerConfigId;
        value.compilerPath=source.compilerPath; value.libraryPath=source.libraryPath; value.debuggerId=source.debuggerId; value.debuggerPath=source.debuggerPath; return value; }
    @Override public void disposeUIResources() { component=null; list=null; toolchainListPanel=null; selected=-1; }
    private record RegistryItem(String id, String name) { @Override public String toString() { return Objects.requireNonNullElse(name, "Unnamed"); } }
    // Kept for migration tests and callers that still infer an SDK home from the old embedded fields.
    static @Nullable String inferLibraryPath(String compilerPath) {
        if (compilerPath == null || compilerPath.isBlank()) return null;
        try { Path compiler=Path.of(compilerPath), parent=compiler.getParent(); return parent!=null&&compiler.toFile().isFile()&&looksLikeSdk(parent)?parent.toString():null; }
        catch (RuntimeException ignored) { return null; }
    }
    static @Nullable String inferCompilerPath(String libraryPath) {
        if (libraryPath == null || libraryPath.isBlank()) return null;
        try { Path root=Path.of(libraryPath); if(!looksLikeSdk(root))return null;Path executable=root.resolve(SystemInfo.isWindows?"odin.exe":"odin");return executable.toFile().isFile()?executable.toString():null; }
        catch (RuntimeException ignored) { return null; }
    }
    private static boolean looksLikeSdk(Path root) { return root.toFile().isDirectory()&&root.resolve("base").toFile().isDirectory(); }

    private final class ToolchainListPanel extends AddDeleteListPanel<OdinToolchainState> {
        private ToolchainListPanel() { super(null, List.of()); }
        @Override protected @Nullable OdinToolchainState findItemToAdd() { return null; }
        @Override protected void customizeDecorator(ToolbarDecorator decorator) {
            decorator.disableUpAction().disableDownAction()
                    .setAddAction(button -> OdinToolchainsConfigurable.this.add())
                    .setRemoveAction(button -> OdinToolchainsConfigurable.this.remove());
        }
        @Override protected ListCellRenderer<? super OdinToolchainState> getListCellRenderer() {
            return (l, value, index, selected, focus) -> new DefaultListCellRenderer()
                    .getListCellRendererComponent(l, Objects.requireNonNullElse(value.name, "Unnamed toolchain"), index, selected, focus);
        }
        private JBList<OdinToolchainState> toolchainList() { return myList; }
        private void addToolchain(OdinToolchainState value) { addElement(value); }
        private void removeAt(int index) { myListModel.remove(index); }
        private void setToolchains(List<OdinToolchainState> values) { myListModel.clear(); for (OdinToolchainState value : values) myListModel.addElement(value); }
    }
}
