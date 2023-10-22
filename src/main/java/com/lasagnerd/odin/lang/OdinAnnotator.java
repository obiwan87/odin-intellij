package com.lasagnerd.odin.lang;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.psi.OdinStringLiteral;
import com.lasagnerd.odin.lang.psi.OdinTypes;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdinAnnotator implements Annotator {

    private static final Pattern ENTIRE_SQ_IS_ESCAPE_SEQ = Pattern.compile("^(" + OdinLangSyntaxAnnotator.ALL_ESCAPE_SEQUENCES + ")$");
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

        if (element instanceof OdinStringLiteral stringLiteral) {
            String text = stringLiteral.getText();
            if (stringLiteral.getDqStringLiteral() != null) {
                if (!text.endsWith("\"") || text.endsWith("\\\"")) {
                    holder.newAnnotation(HighlightSeverity.ERROR, "Unclosed string literal")
                            .highlightType(ProblemHighlightType.GENERIC_ERROR)
                            .range(element.getTextRange())
                            .create();
                }
            }

            if (stringLiteral.getSqStringLiteral() != null) {
                if (!text.endsWith("'") || text.endsWith("\\'")) {
                    holder.newAnnotation(HighlightSeverity.ERROR, "Unclosed rune literal")
                            .highlightType(ProblemHighlightType.GENERIC_ERROR)
                            .range(element.getTextRange())
                            .create();
                } else {
                    if (text.length() > 2) {
                        String textInsideRune = text.substring(1, text.length() - 1);
                        Matcher matcher = ENTIRE_SQ_IS_ESCAPE_SEQ.matcher(textInsideRune);
                        if (!matcher.find() && textInsideRune.codePointCount(0, textInsideRune.length()) > 1) {
                            holder.newAnnotation(HighlightSeverity.ERROR, "Illegal rune literal")
                                    .highlightType(ProblemHighlightType.GENERIC_ERROR)
                                    .range(element.getTextRange())
                                    .create();
                        }
                    }
                }
            }
        }
    }
}
