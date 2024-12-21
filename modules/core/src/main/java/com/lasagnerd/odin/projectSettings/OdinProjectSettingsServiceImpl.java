package com.lasagnerd.odin.projectSettings;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@State(
        name = "com.lasagnerd.odin.settings.OdinSdkConfigPersistentState",
        storages = @Storage("OdinSdkConfig.xml")
)
public class OdinProjectSettingsServiceImpl implements OdinProjectSettingsService {
    @Getter(AccessLevel.NONE)
    private OdinProjectSettingsState state = new OdinProjectSettingsState();

    public @NotNull OdinProjectSettingsState getState() {
        return state;
    }

    @Override
    public boolean isCacheEnabled() {
        if (state.cacheEnabled != null) {
            // Annotator enabled by default
            if (state.cacheEnabled.isEmpty()) {
                return false;
            }
            return state.cacheEnabled.equals("true");
        }
        return true;
    }

    @Override
    public void initializeComponent() {
        OdinProjectSettingsService.super.initializeComponent();
    }

    @Override
    public void loadState(@NotNull OdinProjectSettingsState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    @Override
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

    @Override
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

    @Override
    public boolean isHighlightUnknownReferencesEnabled() {
        if (state.highlightUnknownReferencesEnabled != null) {
            // Annotator enabled by default
            if (state.highlightUnknownReferencesEnabled.isEmpty()) {
                return false;
            }
            return state.highlightUnknownReferencesEnabled.equals("true");
        }
        return true;
    }

    @Override
    public boolean isConditionalSymbolResolutionEnabled() {
        if (state.conditionalSymbolResolutionEnabled != null) {
            // Annotator disabled by default
            if (state.conditionalSymbolResolutionEnabled.isEmpty()) {
                return false;
            }
            return state.conditionalSymbolResolutionEnabled.equals("true");
        }
        return false;
    }
}