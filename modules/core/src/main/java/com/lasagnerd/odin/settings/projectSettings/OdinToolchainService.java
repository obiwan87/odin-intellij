package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OdinToolchainService extends PersistentStateComponent<OdinToolchainRegistryState> {
    static OdinToolchainService getInstance() {
        return ApplicationManager.getApplication().getService(OdinToolchainService.class);
    }

    @NotNull List<OdinToolchainState> getToolchains();

    @Nullable OdinToolchainState find(String id);

    @NotNull OdinToolchainState add(@NotNull OdinToolchainState toolchain);

    @NotNull OdinToolchainState findOrAddEquivalent(@NotNull OdinToolchainState toolchain);

    void replaceToolchains(@NotNull List<OdinToolchainState> toolchains);
}
