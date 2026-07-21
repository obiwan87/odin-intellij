package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@State(
        name = "com.lasagnerd.odin.settings.OdinToolchainRegistry",
        storages = @Storage(value = "odin.toolchains.xml", roamingType = RoamingType.DISABLED)
)
public class OdinToolchainServiceImpl implements OdinToolchainService {
    private final OdinToolchainRegistryState state = new OdinToolchainRegistryState();

    @Override
    public synchronized @NotNull OdinToolchainRegistryState getState() {
        return state;
    }

    @Override
    public synchronized void loadState(@NotNull OdinToolchainRegistryState state) {
        XmlSerializerUtil.copyBean(state, this.state);
        if (this.state.toolchains == null) this.state.toolchains = new ArrayList<>();
    }

    @Override
    public synchronized @NotNull List<OdinToolchainState> getToolchains() {
        return List.copyOf(state.toolchains);
    }

    @Override
    public synchronized @Nullable OdinToolchainState find(String id) {
        if (id == null || id.isBlank()) return null;
        return state.toolchains.stream().filter(it -> id.equals(it.id)).findFirst().orElse(null);
    }

    @Override
    public synchronized @NotNull OdinToolchainState add(@NotNull OdinToolchainState toolchain) {
        if (toolchain.id == null || toolchain.id.isBlank()) toolchain.id = UUID.randomUUID().toString();
        state.toolchains.add(toolchain);
        return toolchain;
    }

    @Override
    public synchronized @NotNull OdinToolchainState findOrAddEquivalent(@NotNull OdinToolchainState candidate) {
        OdinToolchainState existing = state.toolchains.stream()
                .filter(toolchain -> equivalent(toolchain, candidate))
                .findFirst().orElse(null);
        return existing == null ? add(candidate) : existing;
    }

    @Override
    public synchronized void replaceToolchains(@NotNull List<OdinToolchainState> toolchains) {
        state.toolchains = new ArrayList<>(toolchains);
    }

    private static boolean equivalent(OdinToolchainState left, OdinToolchainState right) {
        if ((left.sdkId != null && !left.sdkId.isBlank()) || (right.sdkId != null && !right.sdkId.isBlank()))
            return Objects.equals(left.sdkId, right.sdkId) && Objects.equals(left.debuggerConfigId, right.debuggerConfigId);
        return normalize(left.compilerPath).equals(normalize(right.compilerPath))
                && normalize(left.libraryPath).equals(normalize(right.libraryPath))
                && Objects.equals(left.debuggerId, right.debuggerId)
                && normalize(left.debuggerPath).equals(normalize(right.debuggerPath));
    }

    private static String normalize(String path) {
        if (path == null || path.isBlank()) return "";
        String normalized = FileUtil.toSystemIndependentName(path).replaceAll("/+$", "");
        return SystemInfo.isFileSystemCaseSensitive ? normalized : normalized.toLowerCase(Locale.ROOT);
    }
}
