package com.lasagnerd.odin.rider;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.rider.projectView.views.ProjectViewUtilsKt;
import com.lasagnerd.odin.rider.rootFolders.OdinRiderRootFoldersService;
import org.jetbrains.annotations.NotNull;

public class OdinRiderUnmarkRootAction extends DumbAwareAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (project == null)
            return;

        VirtualFile selection = OdinRiderMarkRootAction.getSelection(e);
        OdinRiderRootFoldersService rootFoldersService = OdinRiderRootFoldersService.getInstance(project);
        if (selection != null && rootFoldersService.isRoot(selection)) {
            e.getPresentation().setEnabled(true);
            e.getPresentation().setVisible(true);
            if (rootFoldersService.isCollectionRoot(selection)) {
                e.getPresentation().setText("Unmark Odin Collection Root");
            }

            if (rootFoldersService.isSourceRoot(selection)) {
                e.getPresentation().setText("Unmark Odin Sources Root");
            }
        } else {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setVisible(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (project == null)
            return;
        VirtualFile selection = OdinRiderMarkSourceRootAction.getSelection(e);
        OdinRiderRootFoldersService rootFoldersService = OdinRiderRootFoldersService.getInstance(project);
        if (selection != null) {
            if (rootFoldersService.isSourceRoot(selection)) {
                rootFoldersService.getState().getSourceRoots().remove(selection.getPath());
            }

            if (rootFoldersService.isCollectionRoot(selection)) {
                rootFoldersService.getState().getCollectionRoots().remove(selection.getPath());
            }
        }

        var projectView = ProjectView.getInstance(project);
        if (projectView != null) {
            ProjectViewUtilsKt.updateAllFromRoot(projectView);
        }
    }
}
