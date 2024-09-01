package com.lasagnerd.odin.debugger.toolchains;

import com.lasagnerd.odin.extensions.OdinDebuggerToolchain;

public class WinDAPToolchain implements OdinDebuggerToolchain {
    @Override
    public String getId() {
        return "odin-win-db-dap";
    }

    @Override
    public String getLabel() {
        return "Windows Debugger";
    }

    @Override
    public String[] getExecutableNames() {
        return new String[]{
                "vsdbg.exe"
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
}
