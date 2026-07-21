package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface OdinSdkRegistryService extends PersistentStateComponent<OdinSdkRegistryState> {
    static OdinSdkRegistryService getInstance() { return ApplicationManager.getApplication().getService(OdinSdkRegistryService.class); }
    @NotNull List<OdinSdkState> getSdks();
    @Nullable OdinSdkState find(String id);
    @NotNull OdinSdkState findOrAddEquivalent(@NotNull OdinSdkState sdk);
    void replaceSdks(@NotNull List<OdinSdkState> sdks);
}
