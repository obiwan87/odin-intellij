package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/** Migrates legacy project toolchain values as soon as a project opens. */
public final class OdinToolchainMigrationActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        OdinProjectToolchainService.getInstance(project).getToolchain();
        migrateRegistry();
    }

    static synchronized void migrateRegistry() {
        OdinToolchainService toolchains = OdinToolchainService.getInstance();
        OdinSdkRegistryService sdks = OdinSdkRegistryService.getInstance();
        OdinDebuggerRegistryService debuggers = OdinDebuggerRegistryService.getInstance();
        boolean changed = false;
        java.util.List<OdinToolchainState> migrated = new java.util.ArrayList<>();
        for (OdinToolchainState source : toolchains.getToolchains()) {
            OdinToolchainState toolchain = copy(source);
            if ((toolchain.sdkId == null || toolchain.sdkId.isBlank())
                    && toolchain.libraryPath != null && !toolchain.libraryPath.isBlank()) {
                OdinSdkState candidate = new OdinSdkState();
                candidate.name = toolchain.name == null || toolchain.name.isBlank() ? "Odin SDK" : toolchain.name + " SDK";
                candidate.compilerPath = java.util.Objects.requireNonNullElse(toolchain.compilerPath, "");
                candidate.libraryPath = toolchain.libraryPath;
                // Do not execute user binaries on the startup thread. Validation populates this when the SDK is edited.
                candidate.version = "";
                toolchain.sdkId = sdks.findOrAddEquivalent(candidate).id;
                changed = true;
            }
            if ((toolchain.debuggerConfigId == null || toolchain.debuggerConfigId.isBlank())
                    && toolchain.debuggerId != null && !toolchain.debuggerId.isBlank()) {
                OdinDebuggerState candidate = new OdinDebuggerState();
                candidate.name = toolchain.name == null || toolchain.name.isBlank() ? "Odin Debugger" : toolchain.name + " Debugger";
                candidate.implementationId = toolchain.debuggerId;
                candidate.executablePath = java.util.Objects.requireNonNullElse(toolchain.debuggerPath, "");
                toolchain.debuggerConfigId = debuggers.findOrAddEquivalent(candidate).id;
                changed = true;
            }
            migrated.add(toolchain);
        }
        if (changed) toolchains.replaceToolchains(migrated);
        toolchains.getState().version = 2;
    }

    private static OdinToolchainState copy(OdinToolchainState source) {
        OdinToolchainState copy = new OdinToolchainState();
        copy.id = source.id; copy.name = source.name; copy.sdkId = source.sdkId;
        copy.debuggerConfigId = source.debuggerConfigId; copy.compilerPath = source.compilerPath;
        copy.libraryPath = source.libraryPath; copy.debuggerId = source.debuggerId; copy.debuggerPath = source.debuggerPath;
        return copy;
    }
}
