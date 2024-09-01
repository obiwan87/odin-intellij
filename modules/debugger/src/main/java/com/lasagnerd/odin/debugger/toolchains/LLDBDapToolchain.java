package com.lasagnerd.odin.debugger.toolchains;

import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.lasagnerd.odin.debugger.drivers.lldbdap.LldbDapDriverConfiguration;
import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;

import java.nio.file.Path;

public class LLDBDapToolchain implements OdinDebuggerToolchain, DebuggerDriverConfigurationProvider {
    public static LLDBDapToolchain INSTANCE = new LLDBDapToolchain();

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
    public boolean isDownloadable() {
        return false;
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public DebuggerDriverConfiguration getDebuggerDriverConfiguration(String path) {
        return new LldbDapDriverConfiguration(Path.of(path));
    }
}
