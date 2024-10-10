package com.lasagnerd.odin.lang.psi;

import com.intellij.codeInsight.highlighting.HighlightedReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.projectSettings.OdinSdkUtils;
import com.lasagnerd.odin.projectStructure.OdinRootTypeUtils;
import com.lasagnerd.odin.projectStructure.collection.OdinRootTypeResult;
import com.lasagnerd.odin.projectStructure.collection.OdinPsiCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public class OdinCollectionReference extends PsiReferenceBase<OdinImportPath> implements HighlightedReference {
    private final TextRange rangeInElement;

    public OdinCollectionReference(@NotNull OdinImportPath element) {
        super(element);
        int index = element.getText().indexOf(":");
        this.rangeInElement = new TextRange(1, index);
    }

    @Override
    public @Nullable PsiElement resolve() {
        return resolveCollectionPathDirectory();
    }

    private @Nullable PsiElement resolveCollectionPathDirectory() {
        OdinImport importInfo = OdinImportUtils.getImportInfo(getElement());
        if (importInfo != null && importInfo.collection() != null) {
            Optional<String> validSdkPath = OdinSdkUtils.getValidSdkPath(getElement().getProject());
            if (validSdkPath.isPresent()) {
                Path collectionPath = Path.of(validSdkPath.get(), importInfo.collection());
                VirtualFile collectionDir = VirtualFileManager.getInstance().findFileByNioPath(collectionPath);
                if (collectionDir != null) {
                    PsiDirectory directory = PsiManager.getInstance(getElement().getProject()).findDirectory(collectionDir);
                    return new OdinPsiCollection(importInfo.collection(), directory);
                }
            }
            OdinRootTypeResult odinRootTypeResult = OdinRootTypeUtils.findCollectionRoot(getElement(), importInfo.collection());
            if (odinRootTypeResult != null && odinRootTypeResult.isCollectionRoot()) {
                VirtualFile collectionDir = odinRootTypeResult.sourceFolder().getFile();
                if (collectionDir != null) {
                    PsiDirectory directory = PsiManager.getInstance(getElement().getProject()).findDirectory(collectionDir);
                    return new OdinPsiCollection(odinRootTypeResult.collectionName(), directory);
                }
            }
        }
        return null;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return rangeInElement;
    }
}
