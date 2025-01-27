package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinCollectionRenameProcessor extends RenamePsiElementProcessor {
    public static void performRename(@NotNull Project project,
                                     PsiDirectory psiDirectory,
                                     String newName) {
        VirtualFile virtualFile = psiDirectory.getVirtualFile();
        OdinRootsService.getInstance(project).renameCollection(virtualFile, newName);
    }

    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof OdinPsiCollection;
    }

    @Override
    public void renameElement(@NotNull PsiElement element, @NotNull String newName, UsageInfo @NotNull [] usages, @Nullable RefactoringElementListener listener) throws IncorrectOperationException {
        super.renameElement(element, newName, usages, listener);
        if(element instanceof OdinPsiCollection odinPsiCollection) {
            performRename(odinPsiCollection.getPsiDirectory().getProject(), odinPsiCollection.getPsiDirectory(), newName);
        }
    }
}
