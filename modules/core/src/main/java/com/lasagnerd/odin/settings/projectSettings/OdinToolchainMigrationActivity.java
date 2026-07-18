package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/** Migrates legacy project toolchain values as soon as a project opens. */
public final class OdinToolchainMigrationActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        OdinProjectToolchainService.getInstance(project).getToolchain();
    }
}
