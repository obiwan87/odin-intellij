package com.lasagnerd.odin.lang;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public class OdinLangSyntaxAnnotator implements Annotator {

    public static final List<String> RESERVED_TYPES = List.of(
            "bool",
            "b8",
            "b16",
            "b32",
            "b64",
            "byte",
            "int",
            "i8",
            "i16",
            "i32",
            "i64",
            "i128",
            "uint",
            "u8",
            "u16",
            "u32",
            "u64",
            "u128",
            "uintptr",
            "i16le",
            "i32le",
            "i64le",
            "i128le",
            "u16le",
            "u32le",
            "u64le",
            "u128le",
            "i16be",
            "i32be",
            "i64be",
            "i128be",
            "u16be",
            "u32be",
            "u64be",
            "u128be",
            "f16",
            "f32",
            "f64",
            "f16le",
            "f32le",
            "f64le",
            "f16be",
            "f32be",
            "f64be",
            "complex32",
            "complex64",
            "complex128",
            "quaternion64",
            "quaternion128",
            "quaternion256",
            "rune",
            "string",
            "cstring",
            "rawptr",
            "typeid",
            "any"
    );


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
            "make",
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

    public static final String ALL_ESCAPE_SEQUENCES =
            "\\\\n|\\\\r|\\\\v|\\\\t|\\\\e|\\\\a|\\\\b|\\\\f|\\\\[0-7]{2}|\\\\x[0-9a-fA-F]{2}|\\\\u[0-9a-fA-F]{4}|\\\\U[0-9a-fA-F]{8}|\\\\\"|\\\\\\\\";
    public static final PsiElementPattern.Capture<PsiElement> IS_CALL_IDENTIFIER = PlatformPatterns.psiElement().withAncestor(2, PlatformPatterns.psiElement(OdinCallExpression.class));
    public static Pattern ESCAPE_SEQUENCES_PATTERN = Pattern.compile(ALL_ESCAPE_SEQUENCES);

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {


        IElementType elementType = PsiUtilCore.getElementType(psiElement);
        if (elementType == OdinTypes.IDENTIFIER_TOKEN) {

            highlightReservedTypes(annotationHolder, psiElement);

            if (predefinedSymbols.contains(psiElement.getText())) {
                TextRange matchRange = psiElement.getTextRange();
                annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(matchRange)
                        .textAttributes(OdinSyntaxHighlighter.BUILTIN_FUNCTION)
                        .create();
            } else if (psiElement.getParent() instanceof OdinIdentifierList list
                    && list.getParent() instanceof OdinConstantInitializationStatement) {
                annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(psiElement.getTextRange())
                        .textAttributes(DefaultLanguageHighlighterColors.CONSTANT)
                        .create();

            }
        }


        highlightEscapeSequences(psiElement, annotationHolder);

        if (psiElement instanceof OdinDirectiveHead tagHead) {
            highlightTagHead(tagHead, annotationHolder);
        }
    }

    private static void highlightTagHead(OdinDirectiveHead tagHead, @NotNull AnnotationHolder annotationHolder) {

        var matchRange = tagHead.getTextRange();
        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(matchRange)
                .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                .create();
    }

    private static void highlightReservedTypes(@NotNull AnnotationHolder annotationHolder, PsiElement identifier) {
        if (identifier != null) {
            if (RESERVED_TYPES.contains(identifier.getText())) {
                TextRange matchRange = identifier.getTextRange();
                annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(matchRange)
                        .textAttributes(DefaultLanguageHighlighterColors.KEYWORD)
                        .create();
            }
        }
    }

    private static void highlightEscapeSequences(PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        IElementType elementType = PsiUtilCore.getElementType(psiElement);
        if (!OdinParserDefinition.STRING_LITERAL_ELEMENTS.contains(elementType)) {
            return;
        }

        var text = psiElement.getText();
        // Find all indexes of escape sequences using regex

        var matcher = ESCAPE_SEQUENCES_PATTERN.matcher(text);
        while (matcher.find()) {
            var matchRange = TextRange.from(
                    psiElement.getTextRange().getStartOffset() + matcher.start(),
                    matcher.end() - matcher.start()
            );
            annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(matchRange)
                    .textAttributes(DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
                    .create();
        }
    }
}
