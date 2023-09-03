package com.lasagnerd.odin.lang;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public class OdinLangSyntaxAnnotator implements Annotator {

    // Check https://pkg.odin-lang.org/core/builtin/ for the full list of built-in types and procedures
    private static final List<String> predefinedSymbols = List.of(
            "len",
            "cap",
            "size_of",
            "align_of",
            "offset_of",
            "offset_of_selector",
            "offset_of_member",
            "offset_of_by_string",
            "type_of",
            "type_info_of",
            "typeid_of",
            "swizzle",
            "complex",
            "quaternion",
            "real",
            "imag",
            "jmag",
            "kmag",
            "conj",
            "expand_values",
            "min",
            "max",
            "abs",
            "clamp",
            "soa_zip",
            "soa_unzip",
            "raw_data"
    );

    static Pattern escapeSequences = Pattern.compile("\\\\n|\\\\r|\\\\v|\\\\t|\\\\e|\\\\a|\\\\b|\\\\f|\\\\[0-7]{2}|\\\\x[0-9a-fA-F]{2}|\\\\u[0-9a-fA-F]{4}|\\\\U[0-9a-fA-F]{8}|\\\\\"|\\\\\\\\");

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        if (psiElement instanceof OdinCallExpression callExpression) {
            highlightBuiltInIdentifiers(annotationHolder, callExpression);
        }

        highlightReservedTypes(annotationHolder, psiElement);


        if (psiElement instanceof OdinStringLiteral stringLiteral) {
            highlightEscapeSequences(stringLiteral, annotationHolder);
        }


        if (psiElement instanceof OdinTagHead tagHead) {
            highlightTagHead(tagHead, annotationHolder);
        }
    }

    private static void highlightTagHead(OdinTagHead tagHead, @NotNull AnnotationHolder annotationHolder) {

        var matchRange = tagHead.getTextRange();
        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(matchRange)
                .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                .create();
    }

    private static void highlightBuiltInIdentifiers(@NotNull AnnotationHolder annotationHolder, OdinCallExpression callExpression) {
        if (callExpression.getCaller().getExpression() instanceof OdinIdentifierExpression identifierExpression) {
            if (predefinedSymbols.contains(identifierExpression.getText())) {
                TextRange matchRange = identifierExpression.getTextRange();
                annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(matchRange)
                        .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                        .create();
            }
        }
    }

    private static void highlightReservedTypes(@NotNull AnnotationHolder annotationHolder, PsiElement psiElement) {
//        PsiElement identifier = null;
//        if(psiElement instanceof OdinTypeDefinitionExpression typeDefinitionExpression) {
//            identifier = typeDefinitionExpression.getIdentifierExpression();
//        } else if(psiElement instanceof OdinIdentifierExpression identifierExpression) {
//            identifier = identifierExpression.getIdentifier();
//        }
//
//        if (identifier != null) {
//            if (reservedTypes.contains(identifier.getText())) {
//                TextRange matchRange = identifier.getTextRange();
//                annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//                        .range(matchRange)
//                        .textAttributes(DefaultLanguageHighlighterColors.KEYWORD)
//                        .create();
//            }
//        }
    }

    private static void highlightEscapeSequences(OdinStringLiteral stringLiteral, @NotNull AnnotationHolder annotationHolder) {
        PsiElement stringElement;
        if (stringLiteral.getDqStringLiteral() != null) {
            stringElement = stringLiteral.getDqStringLiteral();
        } else if (stringLiteral.getSqStringLiteral() != null) {
            stringElement = stringLiteral.getSqStringLiteral();
        } else {
            return;
        }

        var text = stringElement.getText();
        // Find all indexes of escape sequences using regex

        var matcher = escapeSequences.matcher(text);
        while (matcher.find()) {
            var matchRange = TextRange.from(
                    stringElement.getTextRange().getStartOffset() + matcher.start(),
                    matcher.end() - matcher.start()
            );
            annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(matchRange)
                    .textAttributes(DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
                    .create();
        }
    }
}
