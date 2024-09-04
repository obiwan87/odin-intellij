package com.lasagnerd.odin.debugger.toolchains;

import com.intellij.openapi.util.SystemInfo;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriverConfiguration;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;

public class LLDBToolchain implements OdinDebuggerToolchain, DebuggerDriverConfigurationProvider {
    @Override
    public String getId() {
        return "lldb-bundled";
    }

    @Override
    public String getLabel() {
        return "LLDB (Bundled)";
    }

    @Override
    public String[] getExecutableNames() {
        return new String[0];
    }

    @Override
    public boolean isAvailable() {
        return SystemInfo.isMac || SystemInfo.isLinux;
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public DebuggerDriverConfiguration createDebuggerDriverConfiguration(String path) {
        return new LLDBDriverConfiguration();
    }

    @Override
    public boolean isBundled() {
        return true;
    }
}
