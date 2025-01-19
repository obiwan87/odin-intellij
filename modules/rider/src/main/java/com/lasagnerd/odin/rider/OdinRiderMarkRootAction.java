package com.lasagnerd.odin.rider;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.ui.configuration.ModuleSourceRootEditHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.rider.projectView.views.ProjectViewUtilsKt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.util.Locale;

public abstract class OdinRiderMarkRootAction extends DumbAwareAction {
    private static final Logger LOG = Logger.getInstance(OdinRiderMarkSourceRootAction.class);
    @Getter
    protected final JpsModuleSourceRootType<?> rootType;

    public OdinRiderMarkRootAction(@NotNull JpsModuleSourceRootType<?> rootType) {
        super();
        ModuleSourceRootEditHandler<?> editHandler = ModuleSourceRootEditHandler.getEditHandler(rootType);

        LOG.assertTrue(editHandler != null);

        Presentation presentation = this.getTemplatePresentation();
        presentation.setIcon(editHandler.getRootIcon());
        presentation.setText(editHandler.getFullRootTypeName());
        presentation.setDescription(ProjectBundle.messagePointer("module.toggle.sources.action.description",
                editHandler.getFullRootTypeName().toLowerCase(Locale.getDefault())));
        this.rootType = rootType;
    }

    public static VirtualFile getSelection(@NotNull AnActionEvent e) {
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (files == null)
            return null;

        if (files.length != 1)
            return null;

        VirtualFile firstFile = files[0];
        if (!firstFile.isDirectory()) {
            return null;
        }

        return firstFile;
    }

    public static void refresh() {

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (project == null)
            return;
        VirtualFile selection = getSelection(e);
        if (selection != null) {
            markRoot(e, selection);
            ProjectView projectView = ProjectView.getInstance(project);
            if (projectView != null) {
                ProjectViewUtilsKt.updateAllFromRoot(projectView);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        VirtualFile selection = getSelection(e);
        if (selection != null) {
            e.getPresentation().setEnabled(true);
        }
    }

    protected abstract void markRoot(AnActionEvent e, VirtualFile selection);
}
