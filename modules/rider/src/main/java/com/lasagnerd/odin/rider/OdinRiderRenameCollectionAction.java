package com.lasagnerd.odin.rider;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.lasagnerd.odin.codeInsight.imports.OdinCollection;
import com.lasagnerd.odin.projectStructure.collection.OdinRootsService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdinRiderRenameCollectionAction extends DumbAwareAction {
    private static final Logger LOG = Logger.getInstance(OdinRiderRenameCollectionAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        VirtualFile selection = OdinRiderMarkRootAction.getSelection(e);

        LOG.assertTrue(selection != null);
        LOG.assertTrue(project != null);

        OdinRootsService rootsService = OdinRootsService.Companion.getInstance(project);
        OdinCollection collection = rootsService.getCollection(selection);

        LOG.assertTrue(collection != null);

        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.addOkAction();
        dialogBuilder.addCancelAction();
        dialogBuilder.setTitle("Rename Collection");

        // TODO(lasagnerd): Validate user input
        JBTextField collectionName = new JBTextField();
        collectionName.setColumns(25);
        collectionName.setText(collection.name());

        JPanel centerPanel = FormBuilder.createFormBuilder().addLabeledComponent(
                "Collection name: ", collectionName
        ).getPanel();

        dialogBuilder.setCenterPanel(centerPanel);

        boolean ok = dialogBuilder.showAndGet();
        if (ok) {
            rootsService.renameCollection(selection, collectionName.getText());
            OdinRiderUtilsKt.updateSolutionView(project);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        VirtualFile selection = OdinRiderMarkRootAction.getSelection(e);
        if (selection != null && project != null) {
            OdinRootsService rootsService = OdinRootsService.Companion.getInstance(project);
            if (rootsService.isCollectionRoot(selection)) {
                e.getPresentation().setEnabled(true);
                e.getPresentation().setVisible(true);
                return;
            }
        }

        e.getPresentation().setVisible(false);
        e.getPresentation().setEnabled(false);
    }
}
