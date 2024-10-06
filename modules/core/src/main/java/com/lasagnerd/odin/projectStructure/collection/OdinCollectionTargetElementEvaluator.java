package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.codeInsight.TargetElementEvaluatorEx2;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.ThreeState;
import com.lasagnerd.odin.lang.psi.OdinCollectionReference;
import com.lasagnerd.odin.lang.psi.OdinImportPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinCollectionTargetElementEvaluator extends TargetElementEvaluatorEx2 {

    @Override
    public boolean includeSelfInGotoImplementation(@NotNull PsiElement element) {
        return false;
    }

    @Override
    public @Nullable PsiElement getElementByReference(@NotNull PsiReference ref, int flags) {
        if(ref instanceof OdinCollectionReference) {
            PsiElement resolvedElement = ref.resolve();
            if(resolvedElement instanceof OdinPsiCollection psiCollection) {
                return psiCollection.getPsiDirectory();
            }
        }
        return null;
    }

    @Override
    public @NotNull ThreeState isAcceptableReferencedElement(@NotNull PsiElement element, @Nullable PsiElement referenceOrReferencedElement) {
        PsiElement parent = element.getParent();
        if(parent instanceof OdinImportPath && referenceOrReferencedElement instanceof PsiDirectory) {
            return ThreeState.YES;
        }
        return ThreeState.NO;
    }
}
