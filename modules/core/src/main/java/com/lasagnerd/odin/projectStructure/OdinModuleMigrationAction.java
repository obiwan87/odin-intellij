package com.lasagnerd.odin.projectStructure;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class OdinModuleMigrationAction extends NotificationAction {
    private final List<com.intellij.openapi.module.Module> modulesToMigrate;

    public OdinModuleMigrationAction(List<Module> modulesToMigrate) {
        super("Auto-Detect source roots");
        this.modulesToMigrate = modulesToMigrate;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        Project project = e.getProject();
        if (project == null)
            return;
        if (!modulesToMigrate.isEmpty()) {
            OdinProjectMigration.migrateModule(modulesToMigrate.getFirst());
        }
    }
}
