package com.lasagnerd.odin.settings.formatterSettings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsState;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdinFormatterSettingsConfigurable implements Configurable {
    private final Project project;
    private OdinFormatterSettings formatterSettings;

    public OdinFormatterSettingsConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Odin Formatter Settings";
    }

    @Override
    public @Nullable JComponent createComponent() {
        OdinFormatterSettings formatterSettings = new OdinFormatterSettings();
        this.formatterSettings = formatterSettings;
        return formatterSettings.getComponent();
    }

    @Override
    public boolean isModified() {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        boolean odinFmtPathModified = !state.getOdinFmtPath().equals(formatterSettings.getOdinFmtPath());
        boolean odinFmtJsonPathModified = !state.getOdinFmtJsonPath().equals(formatterSettings.getOdinFmtJsonPath());
        boolean useBuiltinFormatterModified = settingsService.isUseBuiltinFormatter() != formatterSettings.isUseBuiltinFormatter();

        return odinFmtPathModified
                | odinFmtJsonPathModified
                | useBuiltinFormatterModified
                ;
    }

    @Override
    public void apply() throws ConfigurationException {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        state.setOdinFmtPath(formatterSettings.getOdinFmtPath());
        state.setUseBuiltinFormatter(String.valueOf(formatterSettings.isUseBuiltinFormatter()));
        state.setOdinFmtJsonPath(formatterSettings.getOdinFmtJsonPath());
    }

    @Override
    public void reset() {
        OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
        OdinProjectSettingsState state = settingsService.getState();

        formatterSettings.setUseBuiltinFormatter(Boolean.parseBoolean(state.getUseBuiltinFormatter()));
        formatterSettings.setOdinFmtPath(state.getOdinFmtPath());
        formatterSettings.setOdinFmtJsonPath(state.getOdinFmtJsonPath());
    }

    @Override
    public void disposeUIResources() {
        this.formatterSettings.dispose();
        this.formatterSettings = null;
    }
}
