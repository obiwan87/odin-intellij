package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.lasagnerd.odin.projectStructure.OdinRootTypeUtils;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import org.jetbrains.annotations.NotNull;

public class OdinCollectionRenameHandler implements RenameHandler {

    @Override
    public boolean isAvailableOnDataContext(@NotNull DataContext dataContext) {
        PsiElement[] psiElementArray = CommonRefactoringUtil.getPsiElementArray(dataContext);

        return psiElementArray.length == 1 &&
                psiElementArray[0] instanceof OdinPsiCollectionDirectory &&
                isCollectionRoot(dataContext.getData(CommonDataKeys.PROJECT),
                        dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
                );
    }

    public static boolean isCollectionRoot(Project project, VirtualFile file) {
        if (project == null)
            return false;

        if (file == null)
            return false;

        if (file.isDirectory()) {
            OdinRootTypeResult result = OdinRootTypeUtils.findCollectionRoot(project, file);
            if (result == null)
                return false;
            return result.sourceFolder() != null
                    && result.sourceFolder().getRootType() == OdinCollectionRootType.INSTANCE;
        }

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
        if(element instanceof OdinPsiCollectionDirectory psiCollectionDirectory) {
            PsiElementRenameHandler.rename(psiCollectionDirectory.getPsiCollection(),
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

