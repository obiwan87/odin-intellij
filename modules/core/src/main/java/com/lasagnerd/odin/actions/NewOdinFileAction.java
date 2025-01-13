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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.lasagnerd.odin.codeInsight.refactor.OdinNameSuggester;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class NewOdinFileAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private static String choosePackageName(PsiDirectory directory, Project project) {
        VirtualFile directoryVirtualFile = directory.getVirtualFile();
        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
        Module moduleForFile = projectFileIndex.getModuleForFile(directoryVirtualFile);
        String packageName = directoryVirtualFile.getName();
        if (moduleForFile != null) {
            List<VirtualFile> sourceRoots = ModuleRootManager.getInstance(moduleForFile)
                    .getSourceRoots(OdinSourceRootType.INSTANCE);
            if (sourceRoots.stream().anyMatch(directoryVirtualFile::equals)) {
                if (directoryVirtualFile.getChildren().length > 0) {
                    for (VirtualFile child : directoryVirtualFile.getChildren()) {
                        if (Objects.equals(child.getExtension(), "odin")) {
                            PsiFile file = PsiManager.getInstance(project).findFile(child);
                            if (file instanceof OdinFile odinFile) {
                                OdinFileScope fileScope = odinFile.getFileScope();
                                if (fileScope != null) {
                                    packageName = fileScope.getPackageClause().getName();
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    packageName = OdinNameSuggester.normalizeName(project.getName());
                }
            }
        }
        return packageName;
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


        if (directory.findFile(fileName) != null) {
            Messages.showErrorDialog(project, "A file with this name already exists.", "Error");
            return;
        }

        final String finalPackageName = choosePackageName(directory, project);

        // run write action
        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
            String content = "package %s\n\n".formatted(finalPackageName);
            PsiFileFactory factory = PsiFileFactory.getInstance(project);
            PsiFile newFile = factory.createFileFromText(finalFileName, OdinFileType.INSTANCE, content);
            directory.add(newFile);

            PsiFile createdFile = directory.findFile(finalFileName);
            if (createdFile != null) {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                fileEditorManager
                        .openFile(createdFile.getVirtualFile(), true);

                FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
                if (selectedEditor instanceof TextEditor textEditor) {
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