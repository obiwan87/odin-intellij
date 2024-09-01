package com.lasagnerd.odin.debugger;

import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.toolchains.DebuggerDriverConfigurationProvider;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;
import com.lasagnerd.odin.sdkConfig.OdinDebuggerSettings;
import com.lasagnerd.odin.sdkConfig.OdinSdkUtils;
import org.jetbrains.annotations.Nullable;

public class OdinDebuggerService {
    private final Project project;

    public OdinDebuggerService(Project project) {
        this.project = project;
    }

    public static OdinDebuggerService getInstance(Project project) {
        return project.getService(OdinDebuggerService.class);
    }

    public DebuggerDriverConfiguration getDebuggerDriverConfiguration() {
        OdinDebuggerSettings debuggerSettings = OdinSdkUtils.getDebuggerSettings(project);
        if (debuggerSettings != null) {
            var debuggerToolchain = getDebuggerToolchain(debuggerSettings);
            if (debuggerToolchain instanceof DebuggerDriverConfigurationProvider debuggerDriverConfigurationProvider) {
                return debuggerDriverConfigurationProvider.getDebuggerDriverConfiguration(debuggerSettings.path());
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
