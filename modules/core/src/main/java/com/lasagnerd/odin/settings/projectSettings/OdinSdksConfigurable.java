package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.fileChooser.*;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.options.*;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.AddDeleteListPanel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.*;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public final class OdinSdksConfigurable implements Configurable {
    private final List<OdinSdkState> items = new ArrayList<>();
    private SdkListPanel sdkListPanel;
    private JBList<OdinSdkState> list;
    private JBTextField name;
    private TextFieldWithBrowseButton compiler, libraries;
    private JLabel version;
    private JBCheckBox check;
    private JPanel component;
    private int selected = -1;
    private boolean updating;

    @Override public @NlsContexts.ConfigurableName String getDisplayName() { return "Odin SDKs"; }

    @Override public @Nullable JComponent createComponent() {
        sdkListPanel = new SdkListPanel();
        list = sdkListPanel.sdkList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && !updating) select(list.getSelectedIndex()); });

        name = new JBTextField();
        compiler = new TextFieldWithBrowseButton();
        compiler.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileDescriptor()));
        libraries = new TextFieldWithBrowseButton();
        libraries.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        version = new JLabel();
        check = new JBCheckBox("Automatically check for new Odin releases");
        JComponent editor = createEditor();
        Splitter splitter = new Splitter(false, 0.3f);
        splitter.setHonorComponentsMinimumSize(false);
        splitter.setFirstComponent(sdkListPanel);
        splitter.setSecondComponent(editor);
        component = new JPanel(new BorderLayout(0, 12)); component.add(splitter, BorderLayout.CENTER); component.add(check, BorderLayout.SOUTH);
        load(); return component;
    }
    private JComponent createEditor() {
        JPanel panel = new JPanel(new GridBagLayout());
        addRow(panel,0,"Name:",name); addRow(panel,1,"Odin executable:",compiler); addRow(panel,2,"Odin libraries:",libraries); addRow(panel,3,"Version:",version);
        GridBagConstraints filler=new GridBagConstraints();filler.gridx=0;filler.gridy=4;filler.gridwidth=2;filler.weightx=1;filler.weighty=1;filler.fill=GridBagConstraints.BOTH;
        panel.add(new JPanel(),filler);panel.setMinimumSize(new Dimension(0,0));return panel;
    }
    private static void addRow(JPanel panel,int row,String text,JComponent field){
        GridBagConstraints label=new GridBagConstraints();label.gridx=0;label.gridy=row;label.anchor=GridBagConstraints.WEST;label.insets=JBUI.insets(0,0,8,8);panel.add(new JLabel(text),label);
        GridBagConstraints value=new GridBagConstraints();value.gridx=1;value.gridy=row;value.weightx=1;value.fill=GridBagConstraints.HORIZONTAL;value.insets=JBUI.insetsBottom(8);
        field.setMinimumSize(new Dimension(0,field.getPreferredSize().height));panel.add(field,value);
    }

    private void showAddMenu(com.intellij.ui.AnActionButton button) {
        DefaultActionGroup actions = new DefaultActionGroup();
        actions.add(new DumbAwareAction("Local SDK") {
            @Override public void actionPerformed(@org.jetbrains.annotations.NotNull AnActionEvent event) { addLocal(); }
        });
        actions.add(new DumbAwareAction("Download SDK") {
            @Override public void actionPerformed(@org.jetbrains.annotations.NotNull AnActionEvent event) { download(); }
        });
        JBPopupFactory.getInstance().createActionGroupPopup(null, actions,
                        DataManager.getInstance().getDataContext(button.getContextComponent()),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true)
                .show(button.getPreferredPopupPoint());
    }

    private void addLocal() {
        OdinLocalSdkDialog dialog = new OdinLocalSdkDialog();
        if (!dialog.showAndGet()) return;
        OdinSdkState value = new OdinSdkState();
        value.id = UUID.randomUUID().toString(); value.name = dialog.sdkName();
        value.compilerPath = dialog.compilerPath(); value.libraryPath = dialog.libraryPath(); value.origin = "LOCAL";
        if (new File(value.compilerPath).isFile()) value.version = OdinSdkUtils.getOdinCompilerVersion(value.compilerPath);
        addToList(value);
    }

    private void download() {
        final List<OdinRelease>[] releases = new List[]{List.of()}; final Exception[] failure = new Exception[1];
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            try { releases[0] = OdinReleaseService.fetchReleases(true).stream().limit(20).toList(); }
            catch (Exception e) { failure[0] = e; }
        }, "Checking for Odin Releases", true, null);
        if (failure[0] != null) { showError("Unable to retrieve Odin releases: " + failure[0].getMessage()); return; }
        if (releases[0].isEmpty()) { showError("GitHub did not return any downloadable Odin releases."); return; }

        OdinDownloadSdkDialog dialog = new OdinDownloadSdkDialog(releases[0]);
        if (!dialog.showAndGet()) return;
        if (dialog.downloadedSdk() != null) addToList(dialog.downloadedSdk());
    }

    private void addToList(OdinSdkState value) {
        flush(); items.add(value); sdkListPanel.addSdk(value);
    }
    private void showError(String text) { JOptionPane.showMessageDialog(component, text, "Odin SDK", JOptionPane.ERROR_MESSAGE); }

    private void select(int index) {
        flush(); selected = index; OdinSdkState value = index >= 0 && index < items.size() ? items.get(index) : null; updating = true;
        name.setText(value == null ? "" : value.name); compiler.setText(value == null ? "" : compilerPath(value));
        libraries.setText(value == null ? "" : libraryPath(value)); version.setText(value == null ? "" : value.version);
        name.setEnabled(value != null); compiler.setEnabled(value != null); libraries.setEnabled(value != null); updating = false;
    }
    private void flush() {
        if (updating || selected < 0 || selected >= items.size()) return;
        OdinSdkState value = items.get(selected); value.name = name.getText(); value.compilerPath = compiler.getText(); value.libraryPath = libraries.getText();
        value.homePath = ""; list.repaint();
    }
    private void remove() {
        int i = list.getSelectedIndex(); if (i < 0) return; OdinSdkState value = items.get(i);
        List<String> affected = OdinToolchainService.getInstance().getToolchains().stream()
                .filter(t -> Objects.equals(t.sdkId, value.id))
                .map(t -> Objects.requireNonNullElse(t.name, "Unnamed toolchain")).toList();
        if (!affected.isEmpty()) {
            String message = "This SDK is used by the following toolchain" + (affected.size() == 1 ? ":\n\n" : "s:\n\n")
                    + String.join("\n", affected) + "\n\nDeleting it will leave "
                    + (affected.size() == 1 ? "this toolchain" : "these toolchains") + " without an SDK. Continue?";
            int answer = JOptionPane.showConfirmDialog(component, message, "Delete SDK in use",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) return;
        }
        selected = -1; items.remove(i); sdkListPanel.removeAt(i); if (items.isEmpty()) select(-1); else list.setSelectedIndex(Math.min(i, items.size() - 1));
    }

    @Override public boolean isModified() {
        flush(); OdinSdkRegistryState state = OdinSdkRegistryService.getInstance().getState();
        return !items.equals(state.sdks) || check.isSelected() != state.automaticallyCheckForReleases;
    }
    @Override public void apply() throws ConfigurationException {
        flush();
        for (OdinSdkState value : items) {
            if (value.name == null || value.name.isBlank()) throw new ConfigurationException("SDK name must not be empty");
            if (!new File(value.compilerPath).isFile()) throw new ConfigurationException("Odin executable does not exist: " + value.compilerPath);
            if (!new File(value.libraryPath).isDirectory()) throw new ConfigurationException("Odin library directory does not exist: " + value.libraryPath);
        }
        OdinSdkRegistryService service = OdinSdkRegistryService.getInstance();
        service.replaceSdks(items.stream().map(OdinSdksConfigurable::copy).toList());
        service.getState().automaticallyCheckForReleases = check.isSelected();
    }
    @Override public void reset() { if (component != null) load(); }
    private void load() {
        updating = true; selected = -1; items.clear();
        items.addAll(OdinSdkRegistryService.getInstance().getSdks().stream().map(OdinSdksConfigurable::copy).toList());
        sdkListPanel.setSdks(items); check.setSelected(OdinSdkRegistryService.getInstance().getState().automaticallyCheckForReleases);
        updating = false; if (items.isEmpty()) select(-1); else list.setSelectedIndex(0);
    }
    private static String compilerPath(OdinSdkState s) { return s.compilerPath == null || s.compilerPath.isBlank() ? safeLegacyCompiler(s.homePath) : s.compilerPath; }
    private static String libraryPath(OdinSdkState s) { return s.libraryPath == null || s.libraryPath.isBlank() ? Objects.requireNonNullElse(s.homePath, "") : s.libraryPath; }
    private static String safeLegacyCompiler(String home) { return home == null || home.isBlank() ? "" : OdinSdkUtils.getOdinBinaryPath(home); }
    private static OdinSdkState copy(OdinSdkState s) {
        OdinSdkState v = new OdinSdkState(); v.id=s.id; v.name=s.name; v.compilerPath=compilerPath(s); v.libraryPath=libraryPath(s); v.version=s.version;
        v.origin=s.origin; v.releaseTag=s.releaseTag; v.releaseId=s.releaseId; v.releaseAssetId=s.releaseAssetId; v.releaseAssetUrl=s.releaseAssetUrl; return v;
    }

    private final class SdkListPanel extends AddDeleteListPanel<OdinSdkState> {
        private SdkListPanel() { super(null, List.of()); }
        @Override protected @Nullable OdinSdkState findItemToAdd() { return null; }
        @Override protected void customizeDecorator(ToolbarDecorator decorator) {
            decorator.disableUpAction().disableDownAction()
                    .setAddAction(OdinSdksConfigurable.this::showAddMenu)
                    .setRemoveAction(button -> OdinSdksConfigurable.this.remove());
        }
        @Override protected ListCellRenderer<? super OdinSdkState> getListCellRenderer() {
            return (l, value, index, selected, focus) -> new DefaultListCellRenderer()
                    .getListCellRendererComponent(l, Objects.requireNonNullElse(value.name, "Unnamed SDK"), index, selected, focus);
        }
        private JBList<OdinSdkState> sdkList() { return myList; }
        private void addSdk(OdinSdkState sdk) { addElement(sdk); }
        private void removeAt(int index) { myListModel.remove(index); }
        private void setSdks(List<OdinSdkState> sdks) { myListModel.clear(); for (OdinSdkState sdk : sdks) myListModel.addElement(sdk); }
    }
}
