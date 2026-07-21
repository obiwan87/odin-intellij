package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AddDeleteListPanel;
import com.intellij.ui.ToolbarDecorator;
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

public final class OdinDebuggersConfigurable implements Configurable {
    public static final String README_DEBUGGER_URL = "https://github.com/obiwan87/odin-intellij#debugger-settings";
    private final OdinDebuggerToolchain[] extensions = OdinDebuggerToolchain.DEBUGGER_TOOLCHAIN.getExtensions();
    private final List<OdinDebuggerState> items = new ArrayList<>();
    private DebuggerListPanel debuggerListPanel;
    private JBList<OdinDebuggerState> list;
    private JBTextField name;
    private JBTextField path;
    private ComboBox<ProviderItem> provider;
    private JPanel component;
    private int selected = -1;
    private boolean updating;

    @Override public @NlsContexts.ConfigurableName String getDisplayName() { return "Debuggers"; }

    @Override public @Nullable JComponent createComponent() {
        if (extensions.length == 0) {
            JButton more = new JButton("Why can't I see any debuggers? Read more");
            more.addActionListener(e -> BrowserUtil.browse(README_DEBUGGER_URL));
            JPanel empty = new JPanel(new GridBagLayout());
            JPanel message = new JPanel(); message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
            message.add(new JLabel("No debugger integrations are available in this IDE."));
            message.add(Box.createVerticalStrut(8)); message.add(more); empty.add(message);
            component = empty; return component;
        }

        debuggerListPanel = new DebuggerListPanel();
        list = debuggerListPanel.debuggerList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && !updating) select(list.getSelectedIndex()); });

        name = new JBTextField(); path = new JBTextField(); provider = new ComboBox<>();
        for (OdinDebuggerToolchain ep : extensions) if (ep.isAvailable()) provider.addItem(new ProviderItem(ep.getId(), ep.getLabel()));
        JButton browse = new JButton("Browse…"); browse.addActionListener(e -> browse());
        JButton download = new JButton("Download"); download.addActionListener(e -> download());
        JPanel pathPanel = new JPanel(new BorderLayout(5, 0)); pathPanel.add(path);
        JPanel pathActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); pathActions.add(browse); pathActions.add(download);
        pathPanel.add(pathActions, BorderLayout.EAST);
        JComponent editor = FormBuilder.createFormBuilder().addLabeledComponent("Name:", name)
                .addLabeledComponent("Debugger:", provider).addLabeledComponent("Executable:", pathPanel)
                .addComponentFillVertically(new JPanel(), 0).getPanel();

        Splitter splitter = new Splitter(false, 0.3f);
        splitter.setFirstComponent(debuggerListPanel); splitter.setSecondComponent(editor);
        component = new JPanel(new BorderLayout()); component.add(splitter);
        load(); return component;
    }

    private void browse() {
        VirtualFile file = FileChooser.chooseFile(FileChooserDescriptorFactory.singleFile(), null, null);
        if (file != null) path.setText(file.getPath());
    }
    private void download() {
        ProviderItem item = (ProviderItem) provider.getSelectedItem();
        OdinDebuggerToolchain ep = item == null ? null : find(item.id);
        if (ep == null || !ep.isDownloadable()) return;
        String downloaded = ep.download(); if (downloaded != null) path.setText(downloaded);
    }
    private void add() {
        flush(); OdinDebuggerState value = new OdinDebuggerState();
        value.id = UUID.randomUUID().toString(); value.name = "Odin Debugger";
        items.add(value); debuggerListPanel.addDebugger(value);
    }
    private void select(int index) {
        flush(); selected = index; OdinDebuggerState value = index >= 0 && index < items.size() ? items.get(index) : null;
        updating = true; name.setText(value == null ? "" : value.name); path.setText(value == null ? "" : value.executablePath);
        if (value != null) for (int i = 0; i < provider.getItemCount(); i++)
            if (Objects.equals(provider.getItemAt(i).id, value.implementationId)) provider.setSelectedIndex(i);
        name.setEnabled(value != null); path.setEnabled(value != null); provider.setEnabled(value != null); updating = false;
    }
    private void flush() {
        if (updating || selected < 0 || selected >= items.size()) return;
        OdinDebuggerState value = items.get(selected); value.name = name.getText(); value.executablePath = path.getText();
        ProviderItem selectedProvider = (ProviderItem) provider.getSelectedItem();
        value.implementationId = selectedProvider == null ? "" : selectedProvider.id; list.repaint();
    }
    private void remove() {
        int index = list.getSelectedIndex(); if (index < 0) return; OdinDebuggerState value = items.get(index);
        if (OdinToolchainService.getInstance().getToolchains().stream().anyMatch(t -> Objects.equals(t.debuggerConfigId, value.id))) {
            JOptionPane.showMessageDialog(component, "This debugger is referenced by one or more toolchains. Reassign those toolchains before removing it.");
            return;
        }
        selected = -1; items.remove(index); debuggerListPanel.removeAt(index);
        if (items.isEmpty()) select(-1); else list.setSelectedIndex(Math.min(index, items.size() - 1));
    }

    @Override public boolean isModified() { if (extensions.length == 0) return false; flush(); return !items.equals(OdinDebuggerRegistryService.getInstance().getDebuggers()); }
    @Override public void apply() throws ConfigurationException {
        if (extensions.length == 0) return; flush();
        for (OdinDebuggerState value : items) {
            if (value.name == null || value.name.isBlank()) throw new ConfigurationException("Debugger name must not be empty");
            OdinDebuggerToolchain ep = find(value.implementationId);
            if (ep == null) throw new ConfigurationException("Debugger integration is unavailable: " + value.implementationId);
            if (!ep.isBundled() && !value.executablePath.isBlank() && !new File(value.executablePath).isFile())
                throw new ConfigurationException("Debugger executable does not exist: " + value.executablePath);
        }
        OdinDebuggerRegistryService.getInstance().replaceDebuggers(items.stream().map(OdinDebuggersConfigurable::copy).toList());
    }
    private OdinDebuggerToolchain find(String id) { for (OdinDebuggerToolchain ep : extensions) if (ep.getId().equals(id)) return ep; return null; }
    @Override public void reset() { if (component != null && extensions.length > 0) load(); }
    private void load() {
        updating = true; selected = -1; items.clear();
        items.addAll(OdinDebuggerRegistryService.getInstance().getDebuggers().stream().map(OdinDebuggersConfigurable::copy).toList());
        debuggerListPanel.setDebuggers(items); updating = false;
        if (items.isEmpty()) select(-1); else list.setSelectedIndex(0);
    }
    private static OdinDebuggerState copy(OdinDebuggerState source) {
        OdinDebuggerState value = new OdinDebuggerState(); value.id=source.id; value.name=source.name;
        value.implementationId=source.implementationId; value.executablePath=source.executablePath; return value;
    }
    private record ProviderItem(String id, String label) { @Override public String toString() { return label; } }

    private final class DebuggerListPanel extends AddDeleteListPanel<OdinDebuggerState> {
        private DebuggerListPanel() { super(null, List.of()); }
        @Override protected @Nullable OdinDebuggerState findItemToAdd() { return null; }
        @Override protected void customizeDecorator(ToolbarDecorator decorator) {
            decorator.disableUpAction().disableDownAction()
                    .setAddAction(button -> OdinDebuggersConfigurable.this.add())
                    .setRemoveAction(button -> OdinDebuggersConfigurable.this.remove());
        }
        @Override protected ListCellRenderer<? super OdinDebuggerState> getListCellRenderer() {
            return (l, value, index, selected, focus) -> new DefaultListCellRenderer()
                    .getListCellRendererComponent(l, Objects.requireNonNullElse(value.name, "Unnamed debugger"), index, selected, focus);
        }
        private JBList<OdinDebuggerState> debuggerList() { return myList; }
        private void addDebugger(OdinDebuggerState value) { addElement(value); }
        private void removeAt(int index) { myListModel.remove(index); }
        private void setDebuggers(List<OdinDebuggerState> values) { myListModel.clear(); for (OdinDebuggerState value : values) myListModel.addElement(value); }
    }
}
