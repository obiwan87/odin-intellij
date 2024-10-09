package com.lasagnerd.odin.lang.psi;

import com.intellij.codeInsight.highlighting.HighlightedReference;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.projectSettings.OdinSdkUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public class OdinPackageReference extends PsiReferenceBase<OdinImportPath> implements HighlightedReference {

    private final Path subdirectory;

    public OdinPackageReference(@NotNull OdinImportPath element, TextRange rangeInElement, Path subdirectory) {
        super(element, rangeInElement);
        this.subdirectory = subdirectory;

    }

    public static @Nullable PsiDirectory resolvePackagePathDirectory(OdinImportPath importPath) {

        OdinImport importInfo = OdinImportUtils.getImportInfo(importPath);
        if (importInfo != null) {
            VirtualFile containingVirtualFile = OdinImportUtils.getContainingVirtualFile(importPath);
            VirtualFile importDir = findDirectoryForImportPath(importPath.getProject(),
                    importInfo,
                    importInfo.path(),
                    containingVirtualFile);
            if (importDir != null) {
                return PsiManager.getInstance(importPath.getProject()).findDirectory(importDir);
            }
        }
        return null;
    }

    private static VirtualFile findDirectoryForImportPath(Project project,
                                                          OdinImport importInfo,
                                                          String importPath,
                                                          VirtualFile containingVirtualFile) {
        if (containingVirtualFile == null)
            return null;

        if (importInfo.collection() != null) {
            Optional<String> sdkPath = OdinSdkUtils.getSdkPath(project);
            if (sdkPath.isPresent()) {
                Path absolutePath = Path.of(sdkPath.get(), importInfo.collection(), importPath).toAbsolutePath();
                if (absolutePath.toFile().exists()) {
                    return VfsUtil.findFile(absolutePath, false);
                }
            }

            Map<String, Path> collectionPaths = OdinImportUtils.getCollectionPaths(project, containingVirtualFile.getPath());
            Path path = collectionPaths.get(importInfo.collection());
            if (path != null) {
                return VfsUtil.findFile(path.resolve(importPath), false);
            }
            return null;
        }

        Path directoryPath = Path.of(
                containingVirtualFile.getParent().getPath(),
                importPath
        );
        return VfsUtil.findFile(directoryPath, false);
    }

    @Override
    public @Nullable PsiElement resolve() {
        VirtualFile containingVirtualFile = OdinImportUtils.getContainingVirtualFile(getElement());
        if(containingVirtualFile != null) {
            OdinImport importInfo = OdinImportUtils.getImportInfo(getElement());
            VirtualFile importDir = findDirectoryForImportPath(getElement().getProject(),
                    importInfo,
                    subdirectory.toString(),
                    containingVirtualFile
            );
            if (importDir != null) {
                return PsiManager.getInstance(getElement().getProject()).findDirectory(importDir);
            }
        }
        return null;
    }
}
