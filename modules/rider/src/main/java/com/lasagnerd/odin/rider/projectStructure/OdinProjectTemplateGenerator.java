package com.lasagnerd.odin.rider.projectStructure;

import com.jetbrains.rider.projectView.projectTemplates.SharedData;
import com.jetbrains.rider.projectView.projectTemplates.generators.ProjectTemplateGenerator;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettings;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdinProjectTemplateGenerator implements ProjectTemplateGenerator {
    @Override
    public @NotNull JComponent getComponent() {
        OdinProjectSettings odinProjectSettings = new OdinProjectSettings();
        return odinProjectSettings.getComponent();
    }

    @Override
    public @Nullable SharedData getSharedData() {
        return null;
    }

    @Override
    public void setSharedData(@NotNull SharedData sharedData) {

    }

    @Override
    public @Nullable Object expandTemplate(@NotNull Continuation<? super Function1<? super Continuation<? super Unit>, ? extends Object>> continuation) {
        return null;
    }
}
