package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import org.jetbrains.annotations.NotNull;

public class OdinRefactoringSupport extends RefactoringSupportProvider {
    @Override
    public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
        return element instanceof OdinDeclaredIdentifier;
    }

    @Override
    public boolean isSafeDeleteAvailable(@NotNull PsiElement element) {
        return element instanceof OdinDeclaredIdentifier;
    }


}
