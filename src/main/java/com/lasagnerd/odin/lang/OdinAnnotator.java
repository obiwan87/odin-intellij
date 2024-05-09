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
import com.lasagnerd.odin.lang.psi.OdinStringLiteral;
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
            if(text.contains("\n") || (text.endsWith("\\\"") || !text.endsWith("\""))) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Line end not allowed inside string literals")
                        .range(element.getTextRange())
                        .highlightType(ProblemHighlightType.GENERIC_ERROR)
                        .create();
            }
        }

        if(elementType == OdinTypes.SQ_STRING_LITERAL) {
            String text = element.getText();
            if(text.contains("\n") || (text.endsWith("\\'") || !text.endsWith("'"))) {
                holder.newAnnotation(HighlightSeverity.ERROR, "Line end not allowed inside rune literals")
                        .range(element.getTextRange())
                        .highlightType(ProblemHighlightType.GENERIC_ERROR)
                        .create();
            }
        }
    }
}
