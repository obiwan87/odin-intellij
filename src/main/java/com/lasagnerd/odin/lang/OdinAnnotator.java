package com.lasagnerd.odin.lang;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdinAnnotator implements Annotator {

    public static final Pattern BLOCK_COMMENT_DELIMITERS = Pattern.compile("/\\*|\\*/");

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiComment comment) {
            IElementType tokenType = comment.getTokenType();
            if (tokenType == OdinTypes.MULTILINE_BLOCK_COMMENT) {

                Matcher matcher = BLOCK_COMMENT_DELIMITERS.matcher(comment.getText());
                int commentNestingDepth = 0;
                while (matcher.find()) {
                    String s = matcher.group();
                    if (s.equals("/*")) {
                        commentNestingDepth++;
                    }
                    if (s.equals("*/")) {
                        commentNestingDepth--;
                    }
                }

                if (commentNestingDepth != 0) {
                    holder.newAnnotation(HighlightSeverity.ERROR, "End of the file is inside comment")
                            .range(TextRange.from(element.getTextOffset() + element.getTextLength() - 1, 1))
                            .highlightType(ProblemHighlightType.GENERIC_ERROR)
                            .create();
                }
            }
        }

        IElementType elementType = PsiUtilCore.getElementType(element);
        if(elementType == OdinTypes.DQ_STRING_LITERAL) {
            String text = element.getText();

            if(text.contains("\n") || isIncorrectlyEndedStringLiteral(text, '"')) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Line end not allowed inside string literals")
                        .range(element.getTextRange())
                        .highlightType(ProblemHighlightType.GENERIC_ERROR)
                        .create();
            }
        }

        if(elementType == OdinTypes.SQ_STRING_LITERAL) {
            String text = element.getText();
            if(text.contains("\n") || isIncorrectlyEndedStringLiteral(text, '\'')) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Line end not allowed inside rune literals")
                        .range(element.getTextRange())
                        .highlightType(ProblemHighlightType.GENERIC_ERROR)
                        .create();
            }
        }
    }

    /**
     * Checks if the provided string is a correctly ended Java string literal.
     * A correctly ended string literal must:
     * - Start and end with a double quote character.
     * - Have all internal double quotes escaped properly.
     *
     * @param literal The string to be checked.
     * @param quoteChar The character used for quotes
     * @return true if the string is a valid Java string literal, false otherwise.
     */
    public static boolean isIncorrectlyEndedStringLiteral(String literal, char quoteChar) {
        if (literal == null || literal.length() < 2) {
            return true;
        }

        // Check if it starts and ends with a quote
        if (literal.charAt(0) != quoteChar || literal.charAt(literal.length() - 1) != quoteChar) {
            return true;
        }

        // Check internal structure for correctly escaped quotes
        for (int i = 1; i < literal.length() - 1; i++) {
            if (literal.charAt(i) == quoteChar) {
                // Check if the preceding character is not an escaping backslash
                // or if it is an escaping backslash, it must itself be escaped
                if (literal.charAt(i - 1) != '\\' || (i > 1 && literal.charAt(i - 1) == '\\' && literal.charAt(i - 2) == '\\')) {
                    return true;
                }
            }
        }

        return false;
    }
}
