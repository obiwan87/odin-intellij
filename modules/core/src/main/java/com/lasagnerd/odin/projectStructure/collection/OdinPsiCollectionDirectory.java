package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.psi.PsiDirectory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;


@Getter
public class OdinPsiCollectionDirectory implements PsiDirectory {
    @Delegate
    private final PsiDirectory psiDirectory;
    private final OdinPsiCollection psiCollection;
    @Setter
    private String collectionName;

    public OdinPsiCollectionDirectory(PsiDirectory psiDirectory, OdinPsiCollection psiCollection) {
        this.psiDirectory = psiDirectory;
        this.psiCollection = psiCollection;
    }
}
