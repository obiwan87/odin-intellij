package com.lasagnerd.odin.rider;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import com.lasagnerd.odin.rider.rootFolders.OdinRootFoldersService;

public class OdinRiderMarkCollectionRootAction extends OdinRiderMarkRootAction {
    public OdinRiderMarkCollectionRootAction() {
        super(OdinCollectionRootType.INSTANCE);
    }

    @Override
    protected void markRoot(AnActionEvent e, VirtualFile selection) {
        Project project = getEventProject(e);
        if (project == null)
            return;

        OdinRootFoldersService rootFoldersService = OdinRootFoldersService.getInstance(project);
        rootFoldersService.getState().getCollectionRoots().put(selection.getPath(), selection.getName());
    }
}
