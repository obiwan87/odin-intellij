package com.lasagnerd.odin.lang;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulator;
import com.intellij.util.IncorrectOperationException;
import com.lasagnerd.odin.lang.psi.OdinIdentifierExpression;
import com.lasagnerd.odin.lang.psi.OdinPrimary;
import com.lasagnerd.odin.lang.psi.OdinPrimaryExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinElementManipulators implements ElementManipulator<OdinIdentifierExpression> {
    @Override
    public @Nullable OdinIdentifierExpression handleContentChange(@NotNull OdinIdentifierExpression element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @Nullable OdinIdentifierExpression handleContentChange(@NotNull OdinIdentifierExpression element, String newContent) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @NotNull TextRange getRangeInElement(@NotNull OdinIdentifierExpression element) {
        return TextRange.from(0, element.getTextLength());
    }
}
