package com.lasagnerd.odin.lang;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulator;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.OdinIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinElementManipulators implements ElementManipulator<OdinIdentifier> {
    @Override
    public @Nullable OdinIdentifier handleContentChange(@NotNull OdinIdentifier element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @Nullable OdinIdentifier handleContentChange(@NotNull OdinIdentifier element, String newContent) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @NotNull TextRange getRangeInElement(@NotNull OdinIdentifier element) {
        return TextRange.from(0, element.getTextLength());
    }
}
