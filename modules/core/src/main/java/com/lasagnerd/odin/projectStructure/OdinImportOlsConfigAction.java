package com.lasagnerd.odin.projectStructure;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.ols.OlsConfiguration;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class OdinImportOlsConfigAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null)
            return;

        VirtualFile selectedFile = getSelectedFile(e);
        if (selectedFile == null)
            return;
        WriteAction.run(
                () -> {
                    FileDocumentManager.getInstance().saveAllDocuments();

                    Module module = ModuleUtilCore.findModuleForFile(selectedFile, project);
                    if (module != null) {
                        VirtualFile contentRootForFile = ProjectFileIndex.getInstance(project).getContentRootForFile(selectedFile);
                        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
                        ModifiableRootModel modifiableModel = moduleRootManager.getModifiableModel();
                        for (ContentEntry contentEntry : modifiableModel.getContentEntries()) {
                            if (Objects.equals(contentEntry.getFile(), contentRootForFile)) {
                                try {
                                    OdinProjectSettingsService settingsService = OdinProjectSettingsService.getInstance(project);
                                    OdinProjectSettingsState state = settingsService.getState();

                                    OdinProjectMigration.importOlsConfiguration(state, contentEntry, selectedFile);
                                    modifiableModel.commit();
                                    settingsService.loadState(state);

                                    Notification odinNotification = NotificationGroupManager.getInstance()
                                            .getNotificationGroup("Odin Notifications")
                                            .createNotification("Config 'ols.json' successfully imported.", NotificationType.INFORMATION);
                                    NotificationsManager.getNotificationsManager().showNotification(odinNotification, project);
                                } catch (Exception ex) {
                                    Notification odinNotification = NotificationGroupManager.getInstance()
                                            .getNotificationGroup("Odin Notifications")
                                            .createNotification("Importing 'ols.json' failed. Incorrect format.", NotificationType.ERROR);

                                    NotificationsManager.getNotificationsManager()
                                            .showNotification(odinNotification, project);
                                }
                                break;
                            }
                        }
                    }
                });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        OlsConfiguration olsConfiguration = getConfigFromSelection(e);
        e.getPresentation().setEnabledAndVisible(olsConfiguration != null);
    }

    @Nullable
    private static OlsConfiguration getConfigFromSelection(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = getSelectedFile(e);
        if (selectedFile == null)
            return null;
        if (selectedFile.getName().equals("ols.json")) {
            return OlsConfiguration.read(selectedFile);
        }
        return null;
    }

    private static VirtualFile getSelectedFile(@NotNull AnActionEvent e) {
        VirtualFile[] selection = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (selection != null && selection.length == 1) {
            return selection[0];
        }
        return null;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
