package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.AddDeleteListPanel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.JBUI;
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
    private TextFieldWithBrowseButton path;
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

        name = new JBTextField();
        path = new TextFieldWithBrowseButton(new ExtendableTextField(10));
        FileChooserDescriptor executableDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        path.addBrowseFolderListener(new TextBrowseFolderListener(executableDescriptor));
        provider = new ComboBox<>();
        for (OdinDebuggerToolchain ep : extensions) if (ep.isAvailable()) provider.addItem(new ProviderItem(ep.getId(), ep.getLabel()));
        JButton download = new JButton("Download"); download.addActionListener(e -> download());
        JComponent editor = createEditor(download);

        Splitter splitter = new Splitter(false, 0.3f);
        splitter.setHonorComponentsMinimumSize(false);
        splitter.setFirstComponent(debuggerListPanel); splitter.setSecondComponent(editor);
        component = new JPanel(new BorderLayout()); component.add(splitter);
        load(); return component;
    }
    private JComponent createEditor(JButton download) {
        JPanel panel=new JPanel(new GridBagLayout());addRow(panel,0,"Name:",name);addRow(panel,1,"Debugger:",provider);addRow(panel,2,"Executable:",path);
        GridBagConstraints action=new GridBagConstraints();action.gridx=1;action.gridy=3;action.anchor=GridBagConstraints.WEST;action.insets=JBUI.insetsBottom(8);panel.add(download,action);
        GridBagConstraints filler=new GridBagConstraints();filler.gridx=0;filler.gridy=4;filler.gridwidth=2;filler.weightx=1;filler.weighty=1;filler.fill=GridBagConstraints.BOTH;
        panel.add(new JPanel(),filler);panel.setMinimumSize(new Dimension(0,0));return panel;
    }
    private static void addRow(JPanel panel,int row,String text,JComponent field){
        GridBagConstraints label=new GridBagConstraints();label.gridx=0;label.gridy=row;label.anchor=GridBagConstraints.WEST;label.insets=JBUI.insets(0,0,8,8);panel.add(new JLabel(text),label);
        GridBagConstraints value=new GridBagConstraints();value.gridx=1;value.gridy=row;value.weightx=1;value.fill=GridBagConstraints.HORIZONTAL;value.insets=JBUI.insetsBottom(8);
        field.setMinimumSize(new Dimension(0,field.getPreferredSize().height));panel.add(field,value);
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
