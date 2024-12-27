package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.Location;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class OdinTestLocator implements SMTestLocator {
    @Override
    public List<Location> getLocation(@NonNls @NotNull String protocol,
                                      @NonNls @NotNull String path,
                                      @NonNls @NotNull Project project,
                                      @NotNull GlobalSearchScope scope) {
        return List.of();
    }

    @Override
    public @NotNull List<Location> getLocation(@NotNull String stacktraceLine, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return SMTestLocator.super.getLocation(stacktraceLine, project, scope);
    }

    @Override
    public @NotNull ModificationTracker getLocationCacheModificationTracker(@NotNull Project project) {
        return SMTestLocator.super.getLocationCacheModificationTracker(project);
    }
}
