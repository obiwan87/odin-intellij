package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

@State(name = "com.lasagnerd.odin.settings.OdinSdkRegistry", storages = @Storage(value = "odin.sdks.xml", roamingType = RoamingType.DISABLED))
public class OdinSdkRegistryServiceImpl implements OdinSdkRegistryService {
    private final OdinSdkRegistryState state = new OdinSdkRegistryState();
    @Override public synchronized @NotNull OdinSdkRegistryState getState() { return state; }
    @Override public synchronized void loadState(@NotNull OdinSdkRegistryState source) {
        XmlSerializerUtil.copyBean(source, state);
        if (state.sdks == null) state.sdks = new ArrayList<>();
    }
    @Override public synchronized @NotNull List<OdinSdkState> getSdks() { return List.copyOf(state.sdks); }
    @Override public synchronized @Nullable OdinSdkState find(String id) {
        return id == null ? null : state.sdks.stream().filter(it -> id.equals(it.id)).findFirst().orElse(null);
    }
    @Override public synchronized @NotNull OdinSdkState findOrAddEquivalent(@NotNull OdinSdkState candidate) {
        OdinSdkState existing = state.sdks.stream().filter(it -> normalize(compilerPath(it)).equals(normalize(compilerPath(candidate)))
                && normalize(libraryPath(it)).equals(normalize(libraryPath(candidate)))).findFirst().orElse(null);
        if (existing != null) return existing;
        if (candidate.id == null || candidate.id.isBlank()) candidate.id = UUID.randomUUID().toString();
        state.sdks.add(candidate);
        return candidate;
    }
    @Override public synchronized void replaceSdks(@NotNull List<OdinSdkState> sdks) { state.sdks = new ArrayList<>(sdks); }
    private static String compilerPath(OdinSdkState sdk) { return sdk.compilerPath == null || sdk.compilerPath.isBlank()
            ? sdk.homePath == null || sdk.homePath.isBlank() ? "" : OdinSdkUtils.getOdinBinaryPath(sdk.homePath) : sdk.compilerPath; }
    private static String libraryPath(OdinSdkState sdk) { return sdk.libraryPath == null || sdk.libraryPath.isBlank() ? Objects.requireNonNullElse(sdk.homePath, "") : sdk.libraryPath; }
    private static String normalize(String path) {
        if (path == null || path.isBlank()) return "";
        String value = FileUtil.toSystemIndependentName(path).replaceAll("/+$", "");
        return SystemInfo.isFileSystemCaseSensitive ? value : value.toLowerCase(Locale.ROOT);
    }
}
