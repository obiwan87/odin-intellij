package com.lasagnerd.odin.projectSettings;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import lombok.Data;

@SuppressWarnings("unused")
@Data
public class OdinProjectSettingsState {
    public String sdkPath = "";
    public String extraBuildFlags = "";
    public String semanticAnnotatorEnabled = "";
    public String odinCheckerEnabled = "";
    public String debuggerId = "";
    public String debuggerPath = "";
    public String highlightUnknownReferencesEnabled = "";
}
