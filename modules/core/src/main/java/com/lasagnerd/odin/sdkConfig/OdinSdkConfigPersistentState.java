package com.lasagnerd.odin.sdkConfig;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@State(
        name = "com.lasagnerd.odin.settings.OdinSdkConfigPersistentState",
        storages = @Storage("OdinSdkConfig.xml")
)
public class OdinSdkConfigPersistentState implements PersistentStateComponent<OdinSdkConfigPersistentState> {
    public String sdkPath = "";
    public String extraBuildFlags = "";
    public String semanticAnnotatorEnabled = "";
    public String odinCheckerEnabled = "";
    public String debuggerId = "";
    public String debuggerPath = "";
    public String highlightUnknownReferencesEnabled = "";

    public static OdinSdkConfigPersistentState getInstance(Project project) {
        return project.getService(OdinSdkConfigPersistentState.class);
    }

    @Override
    public @Nullable OdinSdkConfigPersistentState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull OdinSdkConfigPersistentState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean isSemanticAnnotatorEnabled() {
        if (semanticAnnotatorEnabled != null) {
            // Annotator enabled by default
            if (semanticAnnotatorEnabled.isEmpty()) {
                return true;
            }
            return semanticAnnotatorEnabled.equals("true");
        }
        return false;
    }

    public boolean isOdinCheckerEnabled() {
        if (odinCheckerEnabled != null) {
            // Annotator enabled by default
            if (odinCheckerEnabled.isEmpty()) {
                return true;
            }
            return odinCheckerEnabled.equals("true");
        }
        return false;
    }

    public boolean isHighlightUnknownReferencesEnabled() {
        if (highlightUnknownReferencesEnabled != null) {
            // Annotator enabled by default
            if (highlightUnknownReferencesEnabled.isEmpty()) {
                return false;
            }
            return highlightUnknownReferencesEnabled.equals("true");
        }
        return false;
    }
}