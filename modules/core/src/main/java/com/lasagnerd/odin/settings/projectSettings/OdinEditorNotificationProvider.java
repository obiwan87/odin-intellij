package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Optional;
import java.util.function.Function;

public class OdinEditorNotificationProvider implements EditorNotificationProvider {
    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        return fileEditor -> {

            String odinBinaryPath = OdinSdkUtils.getOdinBinaryPath(project);
            if(odinBinaryPath != null) {
                if(!new File(odinBinaryPath).exists()) {
                    EditorNotificationPanel panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Error);
                    panel.setText("Odin SDK path is not valid");
                    addOpenSettingsAction(project, panel, "Fix", file);
                }
            }
            if(!file.getPath().endsWith(".odin"))
                return null;

            Optional<String> sdkPath = OdinSdkUtils.getSdkPath(project);

            if(sdkPath.isPresent()) {
                return null;
            }

            return createSetupSdkPanel(project, file, fileEditor);
        };
    }

    private static @NotNull EditorNotificationPanel createSetupSdkPanel(@NotNull Project project, @NotNull VirtualFile file, FileEditor fileEditor) {
        EditorNotificationPanel panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning);
        panel.setText("No Odin SDK has been setup for this project");
        String label = "Setup SDK";
        addOpenSettingsAction(project, panel, label, file);
        return panel;
    }

    private static void addOpenSettingsAction(@NotNull Project project, EditorNotificationPanel panel, String label, @NotNull VirtualFile file) {
        panel.createActionLabel(label, () -> {
            if (!project.isDisposed()) {
                file.refresh(false, false);
                ShowSettingsUtil.getInstance().showSettingsDialog(project,
                        OdinProjectConfigurable.class, null);
            }
        });
    }


}
