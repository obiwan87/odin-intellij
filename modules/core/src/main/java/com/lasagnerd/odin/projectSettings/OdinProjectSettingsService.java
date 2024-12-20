package com.lasagnerd.odin.projectSettings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface OdinProjectSettingsService extends PersistentStateComponent<OdinProjectSettingsState> {
    static OdinProjectSettingsService getInstance(Project project) {
        return project.getService(OdinProjectSettingsService.class);
    }

    @Override
    default void initializeComponent() {
        PersistentStateComponent.super.initializeComponent();
    }

    boolean isSemanticAnnotatorEnabled();

    boolean isOdinCheckerEnabled();

    boolean isHighlightUnknownReferencesEnabled();

    boolean isConditionalSymbolResolutionEnabled();

    @NotNull
    OdinProjectSettingsState getState();

    void setState(OdinProjectSettingsState state);
}
