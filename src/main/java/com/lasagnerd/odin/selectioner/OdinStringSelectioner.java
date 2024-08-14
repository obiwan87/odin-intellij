package com.lasagnerd.odin.selectioner;

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.lang.OdinParserDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OdinStringSelectioner extends ExtendWordSelectionHandlerBase {
    @Override
    public boolean canSelect(@NotNull PsiElement e) {
        return OdinParserDefinition.STRING_LITERAL_ELEMENTS.contains(PsiUtilCore.getElementType(e));
    }

    @Override
    public List<TextRange> select(@NotNull PsiElement e, @NotNull CharSequence editorText, int cursorOffset, @NotNull Editor editor) {
        List<TextRange> textRanges = Objects.requireNonNullElse(super.select(e, editorText, cursorOffset, editor), new ArrayList<>());

        TextRange textRange = expandToBeforeQuotes(e);
        textRanges.add(textRange);

        return textRanges;
    }

    /**
     * Expands the selection to just before the quotes in a string literal.
     *
     * @param stringLiteral The PsiElement representing the string literal.
     * @return A TextRange that covers the content of the string without the quotes, or null if not applicable.
     */
    @Nullable
    public static TextRange expandToBeforeQuotes(@NotNull PsiElement stringLiteral) {
        String text = stringLiteral.getText();

        // Check if the string starts and ends with a quote
        if (text.length() >= 2 && isQuote(text.charAt(0))) {
            int startOffset = stringLiteral.getTextRange().getStartOffset();
            int endOffset = stringLiteral.getTextRange().getEndOffset();
            int startContentOffset = startOffset + 1; // Skip the opening quote

            // If the closing quote exists, subtract one from the end offset
            int endContentOffset = isQuote(text.charAt(text.length() - 1))
                    ? endOffset - 1
                    : endOffset; // Include entire text if no closing quote

            if (startContentOffset < endContentOffset) {
                return new TextRange(startContentOffset, endContentOffset);
            }
        }
        return null; // Return null if the element doesn't represent a valid string literal
    }

    /**
     * Checks if the given character is a quote (single or double).
     *
     * @param c The character to check.
     * @return True if the character is a quote, false otherwise.
     */
    private static boolean isQuote(char c) {
        return c == '"' || c == '\'';
    }
}
