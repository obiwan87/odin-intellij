package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

@State(name = "com.lasagnerd.odin.settings.OdinDebuggerRegistry", storages = @Storage(value = "odin.debuggers.xml", roamingType = RoamingType.DISABLED))
public class OdinDebuggerRegistryServiceImpl implements OdinDebuggerRegistryService {
    private final OdinDebuggerRegistryState state = new OdinDebuggerRegistryState();
    @Override public synchronized @NotNull OdinDebuggerRegistryState getState() { return state; }
    @Override public synchronized void loadState(@NotNull OdinDebuggerRegistryState source) {
        XmlSerializerUtil.copyBean(source, state);
        if (state.debuggers == null) state.debuggers = new ArrayList<>();
    }
    @Override public synchronized @NotNull List<OdinDebuggerState> getDebuggers() { return List.copyOf(state.debuggers); }
    @Override public synchronized @Nullable OdinDebuggerState find(String id) {
        return id == null ? null : state.debuggers.stream().filter(it -> id.equals(it.id)).findFirst().orElse(null);
    }
    @Override public synchronized @NotNull OdinDebuggerState findOrAddEquivalent(@NotNull OdinDebuggerState candidate) {
        OdinDebuggerState existing = state.debuggers.stream().filter(it -> Objects.equals(it.implementationId, candidate.implementationId)
                && normalize(it.executablePath).equals(normalize(candidate.executablePath))).findFirst().orElse(null);
        if (existing != null) return existing;
        if (candidate.id == null || candidate.id.isBlank()) candidate.id = UUID.randomUUID().toString();
        state.debuggers.add(candidate);
        return candidate;
    }
    @Override public synchronized void replaceDebuggers(@NotNull List<OdinDebuggerState> debuggers) { state.debuggers = new ArrayList<>(debuggers); }
    private static String normalize(String path) {
        if (path == null || path.isBlank()) return "";
        String value = FileUtil.toSystemIndependentName(path).replaceAll("/+$", "");
        return SystemInfo.isFileSystemCaseSensitive ? value : value.toLowerCase(Locale.ROOT);
    }
}
