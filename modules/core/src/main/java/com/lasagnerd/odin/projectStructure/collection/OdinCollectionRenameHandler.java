package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;

public class OdinCollectionRenameHandler implements RenameHandler {

    @Override
    public boolean isAvailableOnDataContext(@NotNull DataContext dataContext) {
        PsiElement[] psiElementArray = CommonRefactoringUtil.getPsiElementArray(dataContext);

        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        if (project == null || psiElementArray.length != 1) return false;

        VirtualFile virtualFile = dataContext.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile != null)
            return OdinRootsService.getInstance(project).isCollectionRoot(
                    virtualFile
            );

        return false;
    }

    @Override
    public void invoke(@NotNull Project project,
                       Editor editor,
                       PsiFile file,
                       DataContext dataContext) {

    }

    @Override
    public void invoke(@NotNull Project project,
                       PsiElement @NotNull [] elements,
                       DataContext dataContext) {

        PsiElement element = elements[0];
        if (element instanceof PsiDirectory psiCollectionDirectory) {
            VirtualFile virtualFile = psiCollectionDirectory.getVirtualFile();
            var collectionRoot = OdinRootsService.getInstance(project).getCollection(
                    virtualFile
            );
            if (collectionRoot == null)
                return;
            OdinPsiCollection odinPsiCollection = new OdinPsiCollection(collectionRoot.name(), psiCollectionDirectory);
            PsiElementRenameHandler.rename(odinPsiCollection,
                    project,
                    element,
                    null);
        }
    }

    @Override
    public String toString() {
        return "Rename collection";
    }
}

