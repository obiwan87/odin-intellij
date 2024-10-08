package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.ElementManipulator;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.OdinImportPath;
import com.lasagnerd.odin.lang.psi.OdinPsiElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinImportPathManipulator implements ElementManipulator<OdinImportPath> {
    @Override
    public @Nullable OdinImportPath handleContentChange(@NotNull OdinImportPath element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        String importPath = StringUtil.replaceSubstring(element.getText(), range, newContent);

        OdinImportPath newImportPath = OdinPsiElementFactory.getInstance(element.getProject()).createImportPath(importPath);

        return (OdinImportPath) element.replace(newImportPath);
    }

    @Override
    public @Nullable OdinImportPath handleContentChange(@NotNull OdinImportPath element, String newContent) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @NotNull TextRange getRangeInElement(@NotNull OdinImportPath element) {

        String text = element.getText();
        int indexOfColon = text.indexOf(':');
        if (indexOfColon >= 0) {
            return new TextRange(indexOfColon + 1, text.length() - 1);
        }
        return TextRange.allOf(element.getText());
    }
}
