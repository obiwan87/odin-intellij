package com.lasagnerd.odin.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class OdinDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable @Nls String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        return "Hello from docs";
    }


}
