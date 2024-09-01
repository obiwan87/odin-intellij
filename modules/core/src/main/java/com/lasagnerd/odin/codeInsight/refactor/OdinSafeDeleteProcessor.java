package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.safeDelete.NonCodeUsageSearchInfo;
import com.intellij.refactoring.safeDelete.SafeDeleteProcessorDelegate;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class OdinSafeDeleteProcessor implements SafeDeleteProcessorDelegate {
    @Override
    public boolean handlesElement(PsiElement element) {
        return element instanceof OdinDeclaredIdentifier;
    }

    @Override
    public @Nullable NonCodeUsageSearchInfo findUsages(@NotNull PsiElement element, PsiElement @NotNull [] allElementsToDelete, @NotNull List<? super UsageInfo> result) {
        if(element instanceof OdinDeclaredIdentifier declaredIdentifier) {
            ReferencesSearch.search(declaredIdentifier).forEach(reference -> {
                result.add(new UsageInfo(reference));
            });
        }
        return null;
    }

    @Override
    public @Nullable Collection<? extends PsiElement> getElementsToSearch(@NotNull PsiElement element, @NotNull Collection<? extends PsiElement> allElementsToDelete) {
        return List.of(element);
    }

    @Override
    public @Nullable Collection<PsiElement> getAdditionalElementsToDelete(@NotNull PsiElement element, @NotNull Collection<? extends PsiElement> allElementsToDelete, boolean askUser) {
        return List.of();
    }

    @Override
    public @Nullable Collection<@NlsContexts.DialogMessage String> findConflicts(@NotNull PsiElement element, PsiElement @NotNull [] allElementsToDelete) {
        return List.of();
    }

    @Override
    public UsageInfo @Nullable [] preprocessUsages(@NotNull Project project, UsageInfo @NotNull [] usages) {
        return new UsageInfo[0];
    }

    @Override
    public void prepareForDeletion(@NotNull PsiElement element) throws IncorrectOperationException {

    }

    @Override
    public boolean isToSearchInComments(PsiElement element) {
        return false;
    }

    @Override
    public void setToSearchInComments(PsiElement element, boolean enabled) {

    }

    @Override
    public boolean isToSearchForTextOccurrences(PsiElement element) {
        return false;
    }

    @Override
    public void setToSearchForTextOccurrences(PsiElement element, boolean enabled) {

    }
}
