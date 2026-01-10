package com.lasagnerd.odin.lang;

import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsState;
import org.jetbrains.annotations.NotNull;

public class MockProjectSettingsService implements OdinProjectSettingsService {
    OdinProjectSettingsState projectSettingsState;

    public MockProjectSettingsService() {
        projectSettingsState = new OdinProjectSettingsState();
        projectSettingsState.setConditionalSymbolResolutionEnabled("true");
        projectSettingsState.setSdkPath("test/sdk");
    }

    @Override
    public boolean isSemanticAnnotatorEnabled() {
        return false;
    }

    @Override
    public boolean isOdinCheckerEnabled() {
        return false;
    }

    @Override
    public boolean isHighlightUnknownReferencesEnabled() {
        return false;
    }

    @Override
    public boolean isConditionalSymbolResolutionEnabled() {
        return true;
    }

    @Override
    public boolean isUseBuiltinFormatter() {
        return false;
    }

    @Override
    public void setState(OdinProjectSettingsState state) {

    }

    @Override
    public boolean isCacheEnabled() {
        return true;
    }


    @Override
    public @NotNull OdinProjectSettingsState getState() {
        return projectSettingsState;
    }

    @Override
    public void loadState(@NotNull OdinProjectSettingsState state) {

    }
}
