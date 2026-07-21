package com.lasagnerd.odin.settings.projectSettings;

import lombok.Data;

@Data
public class OdinToolchainState {
    public String id = "";
    public String name = "";
    public String sdkId = "";
    /** References an OdinDebuggerState. */
    public String debuggerConfigId = "";
    // Legacy embedded component fields, retained for migration/recovery.
    public String compilerPath = "";
    public String libraryPath = "";
    public String debuggerId = "";
    public String debuggerPath = "";
}
