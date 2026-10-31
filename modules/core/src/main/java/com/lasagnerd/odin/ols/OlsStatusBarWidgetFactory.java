package com.lasagnerd.odin.ols;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;

public class OlsStatusBarWidgetFactory implements StatusBarWidgetFactory {
    private static final String ID = "OdinOlsStatus";

    @Override
    public @NotNull String getId() {
        return ID;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Odin Language Server";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new Widget();
    }

    private static final class Widget implements StatusBarWidget, StatusBarWidget.TextPresentation {
        @Override
        public @NotNull String ID() {
            return ID;
        }

        @Override
        public WidgetPresentation getPresentation() {
            return this;
        }

        @Override
        public @NotNull String getText() {
            return OlsLocator.find().isPresent() ? "OLS" : "OLS: not found";
        }

        @Override
        public float getAlignment() {
            return Component.CENTER_ALIGNMENT;
        }

        @Override
        public String getTooltipText() {
            return OlsLocator.find().map(Object::toString).orElse("Set OLS_PATH or add ols to PATH");
        }
    }
}
