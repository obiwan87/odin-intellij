package com.lasagnerd.odin.refactor;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.lang.psi.OdinDeclaredIdentifier;
import org.jetbrains.annotations.NotNull;

public class OdinRefactorSupport extends RefactoringSupportProvider {
    public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
        return element instanceof OdinDeclaredIdentifier;
    }
}
