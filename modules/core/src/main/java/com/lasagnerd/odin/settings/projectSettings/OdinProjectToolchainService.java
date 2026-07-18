package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class OdinProjectToolchainService {
    private final Project project;

    public OdinProjectToolchainService(Project project) {
        this.project = project;
    }

    public static OdinProjectToolchainService getInstance(Project project) {
        return project.getService(OdinProjectToolchainService.class);
    }

    public synchronized @Nullable OdinToolchainState getToolchain() {
        OdinProjectSettingsState projectState = OdinProjectSettingsService.getInstance(project).getState();
        OdinToolchainService registry = OdinToolchainService.getInstance();
        OdinToolchainState selected = registry.find(projectState.toolchainId);
        if (selected != null) return selected;

        if (!hasLegacyToolchain(projectState)) return null;

        OdinToolchainState candidate = fromLegacy(projectState);
        selected = registry.findOrAddEquivalent(candidate);
        projectState.toolchainId = selected.id;
        return selected;
    }

    public Optional<String> getCompilerPath() {
        return nonBlank(getToolchain(), true);
    }

    public Optional<String> getLibraryPath() {
        return nonBlank(getToolchain(), false);
    }

    public @Nullable OdinDebuggerSettings getDebuggerSettings() {
        OdinToolchainState toolchain = getToolchain();
        if (toolchain == null || toolchain.debuggerId == null || toolchain.debuggerId.isBlank()) return null;
        return new OdinDebuggerSettings(toolchain.debuggerId, toolchain.debuggerPath);
    }

    private static Optional<String> nonBlank(OdinToolchainState toolchain, boolean compiler) {
        if (toolchain == null) return Optional.empty();
        String value = compiler ? toolchain.compilerPath : toolchain.libraryPath;
        if (value == null || value.isBlank()) return Optional.empty();
        try {
            Path.of(value);
            return Optional.of(value);
        } catch (InvalidPathException ignored) {
            return Optional.empty();
        }
    }

    private static boolean hasLegacyToolchain(OdinProjectSettingsState state) {
        return notBlank(state.sdkPath) || notBlank(state.debuggerId) || notBlank(state.debuggerPath);
    }

    private @NotNull OdinToolchainState fromLegacy(OdinProjectSettingsState state) {
        OdinToolchainState toolchain = new OdinToolchainState();
        toolchain.name = uniqueImportedName(project.getName());
        copyLegacyToolchainFields(state, toolchain);
        return toolchain;
    }

    static void copyLegacyToolchainFields(OdinProjectSettingsState state, OdinToolchainState toolchain) {
        toolchain.libraryPath = Objects.requireNonNullElse(state.sdkPath, "");
        toolchain.compilerPath = state.sdkPath == null || state.sdkPath.isBlank()
                ? "" : OdinSdkUtils.getOdinBinaryPath(state.sdkPath);
        toolchain.debuggerId = Objects.requireNonNullElse(state.debuggerId, "");
        toolchain.debuggerPath = Objects.requireNonNullElse(state.debuggerPath, "");
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String uniqueImportedName(String projectName) {
        OdinToolchainService registry = OdinToolchainService.getInstance();
        String base = projectName == null || projectName.isBlank() ? "Imported Odin Toolchain" : "Imported from " + projectName;
        String name = base;
        int suffix = 2;
        while (containsName(registry, name)) {
            name = base + " (" + suffix++ + ")";
        }
        return name;
    }

    private static boolean containsName(OdinToolchainService registry, String name) {
        return registry.getToolchains().stream().anyMatch(it -> name.equals(it.name));
    }
}
