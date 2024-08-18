package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.imports.OdinImportInfo;
import com.lasagnerd.odin.sdkConfig.OdinSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public class OdinImportReference extends PsiReferenceBase<OdinImportPath> {
    public OdinImportReference(@NotNull OdinImportPath element) {
        super(element);
    }


    @Nullable
    @Override
    public PsiElement resolve() {
        VirtualFile importDir = findDirectoryForImportPath(myElement.getProject());

        if (importDir != null) {
            return PsiManager.getInstance(myElement.getProject()).findDirectory(importDir);
        }

        return null;
    }

    private VirtualFile findDirectoryForImportPath(Project project) {
        // Implement this to map the import path to an actual directory in your project.
        OdinImportDeclarationStatement importDeclarationStatement = PsiTreeUtil.getParentOfType(getElement(), OdinImportDeclarationStatement.class);
        if (importDeclarationStatement == null)
            return null;
        OdinImportInfo importInfo = importDeclarationStatement.getImportInfo();


        if (importInfo.library() != null) {
            Optional<String> sdkPath = OdinSdkUtils.getSdkPath(project);
            if (sdkPath.isPresent()) {
                Path absolutePath = Path.of(sdkPath.get(), importInfo.library(), importInfo.path()).toAbsolutePath();
                return VfsUtil.findFile(absolutePath, false);
            } else {
                return null;
            }
        }

        Path directoryPath = Path.of(myElement.getContainingFile().getVirtualFile().getParent().getPath(), importInfo.path());
        return VfsUtil.findFile(directoryPath, false);
    }


}
