package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.projectStructure.OdinProjectUtils;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;

public class OdinCollectionRenameProcessor extends RenamePsiElementProcessor {
    public static void performRename(@NotNull Project project,
                                     PsiDirectory psiDirectory,
                                     String newName) {
        VirtualFile virtualFile = psiDirectory.getVirtualFile();
        Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
        if (module == null)
            return;

        ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
        SourceFolder sourceFolder = OdinProjectUtils.getSourceFolder(virtualFile, modifiableModel);
        if (sourceFolder != null) {
            if (modifiableModel.isDisposed())
                return;

            JpsElement properties = sourceFolder.getJpsElement().getProperties();
            if (properties instanceof OdinCollectionRootProperties rootProperties) {
                rootProperties.setCollectionName(newName);
            }
            WriteAction.run(modifiableModel::commit);
        }
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
