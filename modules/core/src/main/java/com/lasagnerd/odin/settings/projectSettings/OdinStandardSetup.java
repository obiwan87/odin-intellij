package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Picks the toolchain for the "standard" new project preset. Must run on the EDT.
 */
public final class OdinStandardSetup {
    private OdinStandardSetup() {
    }

    /**
     * @return a toolchain id, or an empty string if none is available and the user declined the download
     */
    public static @NotNull String resolveToolchainId() {
        OdinToolchainService toolchains = OdinToolchainService.getInstance();
        OdinSdkRegistryService sdks = OdinSdkRegistryService.getInstance();

        for (OdinToolchainState toolchain : toolchains.getToolchains()) {
            if (sdks.find(toolchain.sdkId) != null) {
                return toolchain.id;
            }
        }

        List<OdinSdkState> installed = sdks.getSdks();
        OdinSdkState sdk = installed.isEmpty() ? downloadLatestStable() : installed.getFirst();
        if (sdk == null) {
            return "";
        }

        OdinToolchainState toolchain = new OdinToolchainState();
        toolchain.id = UUID.randomUUID().toString();
        toolchain.name = "Default Odin toolchain";
        toolchain.sdkId = sdk.id;
        List<OdinDebuggerState> debuggers = OdinDebuggerRegistryService.getInstance().getDebuggers();
        if (!debuggers.isEmpty()) {
            toolchain.debuggerConfigId = debuggers.getFirst().id;
        }
        return toolchains.add(toolchain).id;
    }

    private static @Nullable OdinSdkState downloadLatestStable() {
        int answer = Messages.showYesNoDialog(
                "No Odin SDK is installed. Download the latest stable version?",
                "Odin SDK", "Download", "Skip", Messages.getQuestionIcon());
        if (answer != Messages.YES) {
            return null;
        }

        OdinSdkState[] result = new OdinSdkState[1];
        Exception[] failure = new Exception[1];
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            try {
                for (OdinRelease release : OdinReleaseService.fetchReleases(false)) {
                    OdinRelease.Asset asset = OdinReleaseService.compatibleAsset(release);
                    if (!release.prerelease() && asset != null) {
                        result[0] = OdinSdkDownloader.download(release, asset,
                                OdinSdkDownloader.defaultInstallDirectory(asset.name()),
                                ProgressManager.getInstance().getProgressIndicator());
                        return;
                    }
                }
                failure[0] = new IllegalStateException("No stable release is available for this platform.");
            } catch (Exception e) {
                failure[0] = e;
            }
        }, "Downloading Odin SDK", true, null);

        if (failure[0] != null) {
            Messages.showErrorDialog("Unable to install Odin SDK: " + failure[0].getMessage(), "Odin SDK");
            return null;
        }
        return result[0] == null ? null : OdinSdkRegistryService.getInstance().findOrAddEquivalent(result[0]);
    }
}
