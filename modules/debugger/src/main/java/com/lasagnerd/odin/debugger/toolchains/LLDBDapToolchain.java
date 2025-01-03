package com.lasagnerd.odin.debugger.toolchains;

import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.driverConfigurations.LLDBDAPDriverConfiguration;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;

import java.nio.file.Path;

public class LLDBDapToolchain implements OdinDebuggerToolchain, DebuggerDriverConfigurationProvider {
    private LLDBDapToolchain() {

    }

    @Override
    public String getId() {
        return "odin-lldb-dap";
    }

    @Override
    public String getLabel() {
        return "LLDB DAP";
    }

    @Override
    public String[] getExecutableNames() {
        return new String[]{
                "lldb-dap"
        };
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public DebuggerDriverConfiguration createDebuggerDriverConfiguration(String path) {
        return new LLDBDAPDriverConfiguration(Path.of(path));
    }

}
