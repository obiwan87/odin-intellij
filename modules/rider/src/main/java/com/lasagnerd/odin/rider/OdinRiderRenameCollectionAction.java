package com.lasagnerd.odin.rider;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.lasagnerd.odin.codeInsight.imports.OdinCollection;
import com.lasagnerd.odin.codeInsight.refactor.OdinNameSuggester;
import com.lasagnerd.odin.projectStructure.collection.OdinRootsService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class OdinRiderRenameCollectionAction extends DumbAwareAction {
    private static final Logger LOG = Logger.getInstance(OdinRiderRenameCollectionAction.class);

    private static boolean isCollectionNameValid(String newText) {
        return newText != null && !newText.isBlank() && OdinNameSuggester.isValidIdentifier(newText);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        VirtualFile selection = OdinRiderMarkRootAction.getSelection(e);

        LOG.assertTrue(selection != null);
        LOG.assertTrue(project != null);

        OdinRootsService rootsService = OdinRootsService.getInstance(project);
        OdinCollection collection = rootsService.getCollection(selection);

        LOG.assertTrue(collection != null);

        DialogBuilder dialogBuilder = new DialogBuilder(project);

        dialogBuilder.addOkAction();
        dialogBuilder.addCancelAction();
        dialogBuilder.setTitle("Rename Collection");

        JBTextField collectionName = new JBTextField();
        collectionName.setColumns(25);
        collectionName.setText(collection.name());
        collectionName.getDocument().addDocumentListener(
                new DocumentAdapter() {
                    @Override
                    protected void textChanged(@NotNull DocumentEvent e) {
                        String newText = collectionName.getText();
                        boolean valid = isCollectionNameValid(newText);
                        dialogBuilder.getDialogWrapper().setOKActionEnabled(valid);
                    }
                }
        );

        JPanel centerPanel = FormBuilder.createFormBuilder().addLabeledComponent(
                "Collection name: ", collectionName
        ).getPanel();

        dialogBuilder.setCenterPanel(centerPanel);

        boolean ok = dialogBuilder.showAndGet();
        if (ok && isCollectionNameValid(collectionName.getText())) {
            if (!collectionName.getText().equals(collection.name())) {
                rootsService.renameCollection(selection, collectionName.getText());
                OdinRiderUtilsKt.updateSolutionView(project);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        VirtualFile selection = OdinRiderMarkRootAction.getSelection(e);
        if (selection != null && project != null) {
            OdinRootsService rootsService = OdinRootsService.getInstance(project);
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
