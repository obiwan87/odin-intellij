package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageViewShortNameLocation;
import com.intellij.usageView.UsageViewTypeLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinCollectionTypeDescriptionProvider implements ElementDescriptionProvider {
    @Override
    public @Nullable String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
        if (element instanceof OdinPsiCollection psiCollection) {
            if (location instanceof UsageViewTypeLocation) {
                return "Collection";
            }

            if (location instanceof UsageViewShortNameLocation) {
                return psiCollection.getName();
            }
        }
        return null;
    }
}
