package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.openapi.ui.Queryable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


@Getter
public class OdinPsiCollectionDirectory implements PsiDirectory, Queryable, PsiDirectoryContainer {
    @Delegate
    private final PsiDirectory psiDirectory;
    private final OdinPsiCollection psiCollection;
    @Setter
    private String collectionName;

    public OdinPsiCollectionDirectory(PsiDirectory psiDirectory, OdinPsiCollection psiCollection) {
        this.psiDirectory = psiDirectory;
        this.psiCollection = psiCollection;
    }

    @Override
    public void putInfo(@NotNull Map<? super String, ? super String> info) {
        if (psiDirectory instanceof Queryable q) {
            q.putInfo(info);
        }
    }


    @Override
    public PsiDirectory @NotNull [] getDirectories() {
        return psiDirectory.getSubdirectories();
    }

    @Override
    public PsiDirectory @NotNull [] getDirectories(@NotNull GlobalSearchScope scope) {
        return psiDirectory.getSubdirectories();
    }
}
