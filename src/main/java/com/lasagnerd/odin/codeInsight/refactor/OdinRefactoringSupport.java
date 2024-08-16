package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringActionHandler;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinRefactoringSupport extends RefactoringSupportProvider {
    @Override
    public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
        return element instanceof OdinDeclaredIdentifier;
    }



    @Override
    public boolean isSafeDeleteAvailable(@NotNull PsiElement element) {
        return element instanceof OdinDeclaredIdentifier;
    }

    @Override
    public boolean isAvailable(@NotNull PsiElement context) {
        return super.isAvailable(context);
    }

    @Override
    public @Nullable RefactoringActionHandler getIntroduceVariableHandler(PsiElement element) {
        return new OdinIntroduceVariableHandler(element);
    }

    @Override
    public @Nullable RefactoringActionHandler getIntroduceVariableHandler() {
        return new OdinIntroduceVariableHandler(null);
    }

    public boolean isInplaceIntroduceAvailable(@NotNull PsiElement element, PsiElement context) {
        return true;
    }
}
