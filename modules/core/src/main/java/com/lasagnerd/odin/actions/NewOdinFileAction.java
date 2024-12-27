package com.lasagnerd.odin.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.lasagnerd.odin.lang.OdinFileType;
import org.jetbrains.annotations.NotNull;

public class NewOdinFileAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PsiDirectory directory = event.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiDirectory ?
                (PsiDirectory) event.getData(CommonDataKeys.PSI_ELEMENT) : null;

        if (project == null || directory == null) {
            return;
        }

        String fileName = Messages.showInputDialog(project, "Enter the new Odin file name:", "New Odin File", Messages.getQuestionIcon());
        if (fileName == null || fileName.trim().isEmpty()) {
            return;
        }

        if (!fileName.endsWith(".odin")) {
            fileName += ".odin";
        }

        final String finalFileName = fileName;

        String packageName = directory.getVirtualFile().getName();
        if (directory.findFile(fileName) != null) {
            Messages.showErrorDialog(project, "A file with this name already exists.", "Error");
            return;
        }

        // run write action
        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
            String content = "package %s\n\n".formatted(packageName);
            PsiFileFactory factory = PsiFileFactory.getInstance(project);
            PsiFile newFile = factory.createFileFromText(finalFileName, OdinFileType.INSTANCE, content);
            directory.add(newFile);

            PsiFile createdFile = directory.findFile(finalFileName);
            if (createdFile != null) {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                fileEditorManager
                        .openFile(createdFile.getVirtualFile(), true);

                FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
                if(selectedEditor instanceof TextEditor textEditor) {
                    Editor editor = textEditor.getEditor();
                    editor.getCaretModel().moveToOffset(content.length());
                }

            } else {
                Messages.showErrorDialog(project, "Failed to create file.", "Error");
            }
        }));
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        PsiDirectory directory = event.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiDirectory ?
                (PsiDirectory) event.getData(CommonDataKeys.PSI_ELEMENT) : null;
        event.getPresentation().setEnabledAndVisible(project != null && directory != null);
    }
}