package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.lasagnerd.odin.lang.OdinLanguage;
import com.lasagnerd.odin.projectStructure.OdinRootTypeUtils;
import com.lasagnerd.odin.projectStructure.collection.OdinRootTypeResult;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class OdinMovePackageHandler extends MoveFilesOrDirectoriesHandler {
    @Override
    public boolean canMove(PsiElement[] elements, @Nullable PsiElement targetContainer, @Nullable PsiReference reference) {
        return isValidTarget(targetContainer, elements);
    }

    @Override
    public boolean isValidTarget(@Nullable PsiElement targetElement, PsiElement[] sources) {
        if (!(targetElement instanceof PsiDirectory psiDirectory))
            return false;

        Project project = targetElement.getProject();
        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);

        VirtualFile targetFile = psiDirectory.getVirtualFile();

        OdinRootTypeResult containingRoot = OdinRootTypeUtils.findContainingRoot(project, targetFile);
        if (containingRoot == null)
            return false;

        if (sources.length == 0) {
            return false;
        }

        for (PsiElement sourcePsiElement : sources) {
            if (sourcePsiElement instanceof PsiDirectory sourcePsiDirectory) {
                VirtualFile sourceDirFile = sourcePsiDirectory.getVirtualFile();
                if (projectFileIndex.isInProject(sourceDirFile)) {
                    boolean underSourceRootOfType = projectFileIndex.isUnderSourceRootOfType(sourceDirFile,
                            Set.of(OdinSourceRootType.INSTANCE, OdinCollectionRootType.INSTANCE));

                    // Must be strictly under a root
                    if (underSourceRootOfType) {
                        // Must be contained in a root
                        OdinRootTypeResult sourceRootResult = OdinRootTypeUtils.findContainingRoot(project, sourceDirFile);
                        if (sourceRootResult == null) {
                            return false;
                        }

                        // Not allowed to move to a different source root than the target
                        if (containingRoot.isSourceRoot() && !Objects.equals(containingRoot
                                .directory(), sourceRootResult.directory())) {
                            return false;
                        }

                        // Allowed to move to any other collection or same source root
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canMove(DataContext dataContext) {
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        if (project == null)
            return false;
        Object targetElement = dataContext.getData(DataKey.create("psi.TargetElement"));
        PsiElement[] psiElementArray = CommonRefactoringUtil.getPsiElementArray(dataContext);
        if(targetElement instanceof PsiElement targetPsiElement) {
            return isValidTarget(targetPsiElement, psiElementArray);
        }
        return canMove(project, psiElementArray);
    }

    private static boolean canMove(Project project, PsiElement[] psiElementArray) {

        if (psiElementArray.length == 0)
            return false;

        for (PsiElement psiElement : psiElementArray) {
            if (!isPsiDirectoryUnderSource(project, psiElement)) return false;
        }
        return true;
    }

    private static boolean isPsiDirectoryUnderSource(Project project, PsiElement psiElement) {
        if (psiElement instanceof PsiDirectory psiDirectory) {
            VirtualFile dirFile = psiDirectory.getVirtualFile();
            return isUnderPackageCompatibleSource(project, dirFile);
        } else {
            return false;
        }
    }

    private static boolean isUnderPackageCompatibleSource(Project project, VirtualFile dirFile) {
        return ProjectFileIndex.getInstance(project).isUnderSourceRootOfType(dirFile,
                Set.of(OdinSourceRootType.INSTANCE, OdinCollectionRootType.INSTANCE));
    }


    @Override
    public boolean supportsLanguage(@NotNull Language language) {
        return OdinLanguage.INSTANCE == language;
    }

    @Override
    public boolean isMoveRedundant(PsiElement source, PsiElement target) {
        if (source instanceof PsiDirectory sourceDirectory && target instanceof PsiDirectory targetDirectory) {
            return sourceDirectory.getVirtualFile().getParent().equals(targetDirectory.getVirtualFile());
        }
        return false;
    }

    @Override
    public void collectFilesOrDirsFromContext(DataContext dataContext, Set<PsiElement> filesOrDirs) {
        PsiElement[] psiElementArray = CommonRefactoringUtil.getPsiElementArray(dataContext);
        for (PsiElement psiElement : psiElementArray) {
            if (psiElement instanceof PsiDirectory) {
                filesOrDirs.add(psiElement);
            }
        }
    }
}
