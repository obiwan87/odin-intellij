package com.lasagnerd.odin.debugger;

import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.toolchains.DebuggerDriverConfigurationProvider;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;
import com.lasagnerd.odin.settings.projectSettings.OdinDebuggerSettings;
import com.lasagnerd.odin.settings.projectSettings.OdinSdkUtils;
import org.jetbrains.annotations.Nullable;

public class OdinDebuggerToolchainService {
    private final Project project;

    public OdinDebuggerToolchainService(Project project) {
        this.project = project;
    }

    public static OdinDebuggerToolchainService getInstance(Project project) {
        return project.getService(OdinDebuggerToolchainService.class);
    }

    public DebuggerDriverConfiguration getDebuggerDriverConfiguration() {
        OdinDebuggerSettings debuggerSettings = OdinSdkUtils.getDebuggerSettings(project);
        if (debuggerSettings != null) {
            var debuggerToolchain = getDebuggerToolchain(debuggerSettings);
            if (debuggerToolchain instanceof DebuggerDriverConfigurationProvider debuggerDriverConfigurationProvider) {
                if (debuggerToolchain.isBundled())
                    return debuggerDriverConfigurationProvider.createDebuggerDriverConfiguration(null);

                String path = debuggerSettings.path();
                if (path == null || path.isBlank()) {
                    path = debuggerToolchain.detect();
                }
                if (path != null) {
                    return debuggerDriverConfigurationProvider.createDebuggerDriverConfiguration(path);
                }
            }
        }
        return null;
    }

    private static @Nullable OdinDebuggerToolchain getDebuggerToolchain(OdinDebuggerSettings debuggerSettings) {
        for (OdinDebuggerToolchain extension : OdinDebuggerToolchain.DEBUGGER_TOOLCHAIN.getExtensions()) {
            if (extension.getId().equals(debuggerSettings.id()))
                return extension;
        }
        return null;
    }
}
