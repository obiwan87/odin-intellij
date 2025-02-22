package com.lasagnerd.odin.codeInsight.imports;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public interface OdinImportService {
    static OdinImportService getInstance(Project project) {
        return project.getService(OdinImportService.class);
    }

    String getPackagePath(PsiElement psiElement);

    // Move to OdinImportService
    @Nullable String getCanonicalPath(VirtualFile virtualFile);

    VirtualFile @NotNull [] getFilesInPath(Path importPath);

    PsiFile createPsiFile(VirtualFile virtualFile);

    Optional<String> getSdkPath();

    static String packagePath(PsiElement element) {
        return getInstance(element.getProject()).getPackagePath(element);
    }
}
