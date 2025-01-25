package com.lasagnerd.odin.rider;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import com.lasagnerd.odin.rider.rootFolders.OdinRiderRootFoldersService;
import com.lasagnerd.odin.rider.rootFolders.OdinRootFoldersState;

public class OdinRiderMarkSourceRootAction extends OdinRiderMarkRootAction {
    public OdinRiderMarkSourceRootAction() {
        super(OdinSourceRootType.INSTANCE);
    }

    @Override
    protected void markRoot(AnActionEvent e, VirtualFile selection) {
        Project project = getEventProject(e);
        if (project == null)
            return;

        OdinRiderRootFoldersService rootFoldersService = OdinRiderRootFoldersService.getInstance(project);
        OdinRootFoldersState state = rootFoldersService.getState();
        state.getSourceRoots().add(selection.getPath());
        rootFoldersService.loadState(state);
    }
}
