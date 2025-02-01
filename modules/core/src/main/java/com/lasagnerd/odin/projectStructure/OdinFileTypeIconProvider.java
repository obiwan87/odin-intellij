package com.lasagnerd.odin.projectStructure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.riderInterop.OdinRiderInteropService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdinFileTypeIconProvider implements FileIconProvider {
    @Override
    public @Nullable Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        if (!file.isDirectory())
            return null;
        if (project == null)
            return null;

        if (OdinRiderInteropService.isRider(project)) {
            if (OdinRiderInteropService.getInstance(project).isCollectionRoot(file)) {
                return AllIcons.Nodes.PpLibFolder;
            }

            if (OdinRiderInteropService.getInstance(project).isSourceRoot(file)) {
                return AllIcons.Modules.SourceRoot;
            }
        }

        boolean underSourceRootOfType = OdinImportUtils.isUnderSourceRoot(project, file);
        if (underSourceRootOfType) {
            return AllIcons.Nodes.Package;
        }


        return null;
    }
}
