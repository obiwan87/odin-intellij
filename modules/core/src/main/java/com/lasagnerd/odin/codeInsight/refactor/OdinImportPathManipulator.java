package com.lasagnerd.odin.codeInsight.refactor;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulator;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.OdinImportPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinImportPathManipulator implements ElementManipulator<OdinImportPath> {
    @Override
    public @Nullable OdinImportPath handleContentChange(@NotNull OdinImportPath element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @Nullable OdinImportPath handleContentChange(@NotNull OdinImportPath element, String newContent) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @NotNull TextRange getRangeInElement(@NotNull OdinImportPath element) {
        return TextRange.allOf(element.getText());
    }
}
