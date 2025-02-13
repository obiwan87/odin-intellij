package com.lasagnerd.odin.runConfiguration.test;

import com.intellij.execution.Location;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.lang.psi.OdinFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("rawtypes")
class OdinTestLocator implements SMTestLocator {
    @Override
    public @NotNull List<Location> getLocation(@NonNls @NotNull String protocol,
                                               @NonNls @NotNull String path,
                                               @NonNls @NotNull Project project,
                                               @NotNull GlobalSearchScope scope) {
        String[] split = path.split("#");
        if (split.length == 2) {
            String filePath = split[0];
            String qualifiedProcedureName = split[1];
            int index = qualifiedProcedureName.indexOf('.');
            if (index >= 0) {
                String procedureName = qualifiedProcedureName.substring(index + 1);
                VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(filePath));
                if (virtualFile != null) {
                    PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
                    if (file instanceof OdinFile odinFile) {
                        OdinSymbolTable symbolTable = odinFile.getFileScope().getSymbolTable();
                        OdinSymbol symbol = symbolTable.getSymbol(procedureName);
                        if (symbol != null && symbol.getDeclaredIdentifier() != null) {
                            return Collections.singletonList(new OdinLocation(symbol.getDeclaredIdentifier(), project));
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<Location> getLocation(@NotNull String stacktraceLine, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return SMTestLocator.super.getLocation(stacktraceLine, project, scope);
    }

    @Override
    public @NotNull ModificationTracker getLocationCacheModificationTracker(@NotNull Project project) {
        return SMTestLocator.super.getLocationCacheModificationTracker(project);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    private static class OdinLocation extends Location<PsiElement> {
        PsiElement psiElement;
        Project project;

        @Override
        public @NotNull <T extends PsiElement> Iterator<Location<T>> getAncestors(Class<T> ancestorClass, boolean strict) {
            return Collections.emptyIterator();
        }

        @Override
        public @Nullable Module getModule() {
            return null;
        }
    }
}
