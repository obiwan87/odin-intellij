package com.lasagnerd.odin.lang.psi;

import com.intellij.codeInsight.highlighting.HighlightedReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.lasagnerd.odin.codeInsight.imports.OdinImportInfo;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.projectStructure.collection.OdinCollectionSearchResult;
import com.lasagnerd.odin.projectStructure.OdinProjectUtils;
import com.lasagnerd.odin.projectStructure.collection.OdinPsiCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        OdinImportInfo importInfo = OdinImportUtils.getImportInfo(getElement());
        if (importInfo != null && importInfo.collection() != null) {
            OdinCollectionSearchResult odinCollectionSearchResult = OdinProjectUtils.findOdinCollection(getElement(), importInfo.collection());
            if (odinCollectionSearchResult != null) {
                VirtualFile collectionDir = odinCollectionSearchResult.sourceFolder().getFile();
                if (collectionDir != null) {
                    PsiDirectory directory = PsiManager.getInstance(getElement().getProject()).findDirectory(collectionDir);
                    return new OdinPsiCollection(odinCollectionSearchResult.collectionName(), directory);
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
