package com.lasagnerd.odin.settings.projectSettings;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class OdinDebuggerRegistryState {
    public int version = 1;
    public List<OdinDebuggerState> debuggers = new ArrayList<>();
}
