package com.lasagnerd.odin.settings.projectSettings;

import lombok.Data;

@Data
public class OdinToolchainState {
    public String id = "";
    public String name = "";
    public String compilerPath = "";
    public String libraryPath = "";
    public String debuggerId = "";
    public String debuggerPath = "";
}
