package com.lasagnerd.odin.lang;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulator;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.OdinPrimary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinElementManipulators implements ElementManipulator<OdinPrimary> {
    @Override
    public @Nullable OdinPrimary handleContentChange(@NotNull OdinPrimary element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @Nullable OdinPrimary handleContentChange(@NotNull OdinPrimary element, String newContent) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @NotNull TextRange getRangeInElement(@NotNull OdinPrimary element) {
        return TextRange.from(0, element.getTextLength());
    }
}
