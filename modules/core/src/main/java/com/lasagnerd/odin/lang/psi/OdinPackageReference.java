package com.lasagnerd.odin.lang.psi;

import com.intellij.codeInsight.highlighting.HighlightedReference;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.lasagnerd.odin.codeInsight.imports.OdinImportInfo;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.projectSettings.OdinSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class OdinPackageReference extends PsiReferenceBase<OdinImportPath> implements HighlightedReference {
    private final TextRange rangeInElement;

    public OdinPackageReference(@NotNull OdinImportPath element) {
        super(element);
        int index = element.getText().indexOf(":");
        if (index < 0) {
            index = 1;
        } else {
            index += 1;
        }

        this.rangeInElement = new TextRange(index, element.getText().length() - 1);
    }

    public static @Nullable PsiDirectory resolvePackagePathDirectory(OdinImportPath importPath) {
        VirtualFile importDir = findDirectoryForImportPath(importPath.getProject(), importPath);

        if (importDir != null) {
            return PsiManager.getInstance(importPath.getProject()).findDirectory(importDir);
        }

        return null;
    }

    private static VirtualFile findDirectoryForImportPath(Project project, @NotNull OdinImportPath element) {
        // Implement this to map the import path to an actual directory in your project.
        OdinImportInfo importInfo = OdinImportUtils.getImportInfo(element);
        if (importInfo == null) return null;


        if (importInfo.collection() != null) {
            Optional<String> sdkPath = OdinSdkUtils.getSdkPath(project);
            if (sdkPath.isPresent()) {
                Path absolutePath = Path.of(sdkPath.get(), importInfo.collection(), importInfo.path()).toAbsolutePath();
                if (absolutePath.toFile().exists()) {
                    return VfsUtil.findFile(absolutePath, false);
                }
            }
            VirtualFile containingVirtualFile = OdinImportUtils.getContainingVirtualFile(element);
            if (containingVirtualFile != null) {
                Map<String, Path> collectionPaths = OdinImportUtils.getCollectionPaths(project, containingVirtualFile.getPath());
                Path path = collectionPaths.get(importInfo.collection());
                if (path != null) {
                    return VfsUtil.findFile(path.resolve(importInfo.path()), false);
                }
            }
            return null;
        }

        Path directoryPath = Path.of(element.getContainingFile().getVirtualFile().getParent().getPath(), importInfo.path());
        return VfsUtil.findFile(directoryPath, false);
    }

    @Override
    public @Nullable PsiElement resolve() {
        return resolvePackagePathDirectory(myElement);
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return rangeInElement;
    }

    //    @Override
//    public Object @NotNull [] getVariants() {
//        OdinImportInfo importInfo = OdinImportUtils.getImportInfo(getElement());
//        if (importInfo == null)
//            return new Object[0];
//
//        if (importInfo.collection() == null) {
//            return new Object[]{importInfo.packageName()};
//        }
//
//        return new Object[]{importInfo.collection(), importInfo.packageName()};
//    }
}
