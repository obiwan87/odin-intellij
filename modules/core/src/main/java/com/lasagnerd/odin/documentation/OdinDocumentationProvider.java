package com.lasagnerd.odin.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class OdinDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable @Nls String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        String text = originalElement != null? originalElement.getText() : "null";
        return "element: %s<br> original: %s".formatted(element.getText(), text);
    }


}
