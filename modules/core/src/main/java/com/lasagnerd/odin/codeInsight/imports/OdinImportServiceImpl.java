package com.lasagnerd.odin.codeInsight.imports;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.sdkConfig.OdinSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public class OdinImportServiceImpl implements OdinImportService {
    private final Project project;

    public OdinImportServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public String getPackagePath(PsiElement psiElement) {
        OdinFile containingFile = (OdinFile) psiElement.getContainingFile();
        if (containingFile == null)
            return null;
        @NotNull PsiFile virtualFile = containingFile.getOriginalFile();
        PsiDirectory containingDirectory = virtualFile.getContainingDirectory();
        if (containingDirectory != null) {
            return containingDirectory.getVirtualFile().getPath();
        }
        return null;
    }

    @Override
    public @Nullable String getCanonicalPath(VirtualFile virtualFile) {
        return virtualFile.getCanonicalPath();
    }

    public VirtualFile @NotNull [] getFilesInPath(Path importPath) {
        VirtualFile packageDirectory = VfsUtil.findFile(importPath, true);
        VirtualFile[] children = null;
        if (packageDirectory != null) {
            children = packageDirectory.getChildren();
        }
        if(children == null)
            return new VirtualFile[0];
        return children;
    }

    @Override
    public PsiFile createPsiFile(VirtualFile virtualFile) {
        return this.project.getService(PsiManager.class).findFile(virtualFile);
    }

    @Override
    public Optional<String> getSdkPath() {
        return OdinSdkUtils.getSdkPath(project);
    }
}
