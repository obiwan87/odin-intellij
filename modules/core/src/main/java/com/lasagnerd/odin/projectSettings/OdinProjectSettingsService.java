package com.lasagnerd.odin.projectSettings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.AccessLevel;
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
public class OdinProjectSettingsService implements PersistentStateComponent<OdinProjectSettingsState> {
    @Getter(AccessLevel.NONE)
    private OdinProjectSettingsState state = new OdinProjectSettingsState();

    public static OdinProjectSettingsService getInstance(Project project) {
        return project.getService(OdinProjectSettingsService.class);
    }

    public @NotNull OdinProjectSettingsState getState() {
        return state;
    }

    @Override
    public void initializeComponent() {
        PersistentStateComponent.super.initializeComponent();
    }

    @Override
    public void loadState(@NotNull OdinProjectSettingsState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    public boolean isSemanticAnnotatorEnabled() {
        if (state.semanticAnnotatorEnabled != null) {
            // Annotator enabled by default
            if (state.semanticAnnotatorEnabled.isEmpty()) {
                return true;
            }
            return state.semanticAnnotatorEnabled.equals("true");
        }
        return false;
    }

    public boolean isOdinCheckerEnabled() {
        if (state.odinCheckerEnabled != null) {
            // Annotator enabled by default
            if (state.odinCheckerEnabled.isEmpty()) {
                return true;
            }
            return state.odinCheckerEnabled.equals("true");
        }
        return false;
    }

    public boolean isHighlightUnknownReferencesEnabled() {
        if (state.highlightUnknownReferencesEnabled != null) {
            // Annotator enabled by default
            if (state.highlightUnknownReferencesEnabled.isEmpty()) {
                return false;
            }
            return state.highlightUnknownReferencesEnabled.equals("true");
        }
        return false;
    }
}