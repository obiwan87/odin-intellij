package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface OdinDebuggerRegistryService extends PersistentStateComponent<OdinDebuggerRegistryState> {
    static OdinDebuggerRegistryService getInstance() { return ApplicationManager.getApplication().getService(OdinDebuggerRegistryService.class); }
    @NotNull List<OdinDebuggerState> getDebuggers();
    @Nullable OdinDebuggerState find(String id);
    @NotNull OdinDebuggerState findOrAddEquivalent(@NotNull OdinDebuggerState debugger);
    void replaceDebuggers(@NotNull List<OdinDebuggerState> debuggers);
}
