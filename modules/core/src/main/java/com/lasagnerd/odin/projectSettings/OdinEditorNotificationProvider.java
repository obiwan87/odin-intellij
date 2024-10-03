package com.lasagnerd.odin.projectSettings;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Optional;
import java.util.function.Function;

public class OdinEditorNotificationProvider implements EditorNotificationProvider {
    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        return fileEditor -> {
            if(!file.getPath().endsWith(".odin"))
                return null;

            Optional<String> sdkPath = OdinSdkUtils.getSdkPath(project);

            if(sdkPath.isPresent()) {
                if(!OdinSdkLibraryManager.isSdkLibraryConfigured(project)) {
                    EditorNotificationPanel panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning);
                    panel.setText("Odin SDK is not configured as project library");
                    panel.createActionLabel("Set library", () -> {
                        if (!project.isDisposed()) {
                            file.refresh(false, false);
                            OdinSdkLibraryManager.addOrUpdateOdinSdkLibrary(project, sdkPath.get());
                        }
                    });
                    return panel;
                }
                return null;
            }

            EditorNotificationPanel panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning);
            panel.setText("No Odin SDK has been setup for this project");
            panel.createActionLabel("Setup SDK", () -> {
                if (!project.isDisposed()) {
                    file.refresh(false, false);
                    ShowSettingsUtil.getInstance().showSettingsDialog(project,
                            OdinProjectConfigurable.class, null);
                }
            });

            return panel;
        };
    }


}
