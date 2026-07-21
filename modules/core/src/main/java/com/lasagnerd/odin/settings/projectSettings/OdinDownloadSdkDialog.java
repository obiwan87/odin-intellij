package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.util.ui.FormBuilder;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.nio.file.Path;
import java.util.List;

final class OdinDownloadSdkDialog extends DialogWrapper {
    private final ComboBox<OdinRelease> versions = new ComboBox<>();
    private final ComboBox<OdinRelease.Asset> platforms = new ComboBox<>();
    private final TextFieldWithBrowseButton location = new TextFieldWithBrowseButton();
    private OdinSdkState downloadedSdk;

    OdinDownloadSdkDialog(List<OdinRelease> releases) {
        super(true);
        setTitle("Download Odin SDK");
        releases.stream().limit(20).forEach(versions::addItem);
        versions.addItemListener(e -> { if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) updateAssets(); });
        platforms.setRenderer((list, value, index, selected, focus) -> new DefaultListCellRenderer()
                .getListCellRendererComponent(list, value == null ? "" : platformLabel(value.name()), index, selected, focus));
        platforms.addItemListener(e -> { if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) updateDefaultLocation(); });
        location.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        updateAssets();
        init();
        setOKButtonText("Download");
    }
    @Override protected @Nullable JComponent createCenterPanel() {
        return FormBuilder.createFormBuilder().addLabeledComponent("Version:", versions)
                .addLabeledComponent("OS / architecture:", platforms)
                .addLabeledComponent("Download location:", location).getPanel();
    }
    private void updateAssets() {
        platforms.removeAllItems();
        OdinRelease release = selectedRelease();
        if (release != null && release.assets() != null) release.assets().stream()
                .filter(asset -> OdinReleaseService.isSdkArchive(asset.name())).forEach(platforms::addItem);
        if (platforms.getItemCount() > 0) platforms.setSelectedIndex(0);
        updateDefaultLocation();
    }
    private void updateDefaultLocation() {
        OdinRelease.Asset asset = selectedAsset();
        if (asset == null) return;
        location.setText(OdinSdkDownloader.defaultInstallDirectory(asset.name()).toString());
    }
    OdinRelease selectedRelease() { return (OdinRelease) versions.getSelectedItem(); }
    OdinRelease.Asset selectedAsset() { return (OdinRelease.Asset) platforms.getSelectedItem(); }
    Path destination() { return Path.of(location.getText()); }
    OdinSdkState downloadedSdk() { return downloadedSdk; }

    static String platformLabel(String assetName) {
        String[] parts = assetName.split("-");
        if (parts.length < 3) return assetName;
        String os = switch (parts[1].toLowerCase()) { case "windows" -> "Windows"; case "linux" -> "Linux"; case "macos" -> "macOS"; default -> parts[1]; };
        return os + " / " + parts[2].toUpperCase();
    }

    @Override protected void doOKAction() {
        OdinRelease release = selectedRelease(); OdinRelease.Asset asset = selectedAsset();
        if (release == null || asset == null) { setErrorText("Select a release and an OS / architecture."); return; }
        final Exception[] failure = new Exception[1];
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            try { downloadedSdk = OdinSdkDownloader.download(release, asset, destination(), ProgressManager.getInstance().getProgressIndicator()); }
            catch (Exception e) { failure[0] = e; }
        }, "Downloading " + asset.name(), true, null);
        if (failure[0] != null) { setErrorText("Unable to install Odin SDK: " + failure[0].getMessage()); return; }
        super.doOKAction();
    }
}
