package com.lasagnerd.odin.projectStructure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

public class OdinFileTypeIconProvider implements FileIconProvider {
    @Override
    public @Nullable Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        if(!file.isDirectory())
            return null;
        if(project == null)
            return null;

        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
        if (projectFileIndex.isUnderSourceRootOfType(file, Set.of(OdinSourceRootType.INSTANCE, OdinCollectionRootType.INSTANCE))) {
            return AllIcons.Nodes.Package;
        }
        return null;
    }
}
