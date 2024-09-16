package com.lasagnerd.odin.codeInsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImportInfo;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.lang.OdinParserDefinition;
import com.lasagnerd.odin.lang.OdinSyntaxHighlighter;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

import static com.lasagnerd.odin.lang.psi.OdinReference.logStackOverFlowError;

public class OdinLangHighlightingAnnotator implements Annotator {
    public static Logger LOG = Logger.getInstance(OdinLangHighlightingAnnotator.class);
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
            "any",
            "true",
            "false",
            "nil"
    );


    // Check https://pkg.odin-lang.org/core/builtin/ for the full list of built-in types and procedures
    private static final List<String> PREDEFINED_SYMBOLS = List.of(
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
    public static final @NotNull Key<Object> ANNOTATION_SESSION_STATE = Key.create("annotationSessionState");
    public static Pattern ESCAPE_SEQUENCES_PATTERN = Pattern.compile(ALL_ESCAPE_SEQUENCES);

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        OdinSdkConfigPersistentState persistentState = OdinSdkConfigPersistentState.getInstance(annotationHolder.getCurrentAnnotationSession().getFile().getProject());
        if (persistentState.getState() != null && !persistentState.getState().isSemanticAnnotatorEnabled())
            return;

        IElementType elementType = PsiUtilCore.getElementType(psiElement);

        TextRange psiElementRange = psiElement.getTextRange();

        if (elementType == OdinTypes.IMPORT_PATH) {
            OdinImportPath odinImportPath = (OdinImportPath) psiElement;
            OdinImportDeclarationStatement parent = (OdinImportDeclarationStatement) odinImportPath.getParent();
            OdinImportInfo importInfo = parent.getImportInfo();
            if (importInfo.library() != null) {
                String text = odinImportPath.getText();
                int indexOfColon = text.indexOf(':');
                if (indexOfColon > 2) {

                    annotationHolder
                            .newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(TextRange.from(1 + psiElementRange.getStartOffset(), indexOfColon - 1))
                            .textAttributes(OdinSyntaxHighlighter.LIBRARY)
                            .create();
                }
            }
        }

        if (elementType == OdinTypes.IDENTIFIER_TOKEN) {

            String identifierText = psiElement.getText();

            if (RESERVED_TYPES.contains(identifierText)) {
                highlight(annotationHolder, psiElementRange, OdinSyntaxHighlighter.BUILTIN_FUNCTION);
            } else if (PREDEFINED_SYMBOLS.contains(identifierText)) {
                highlight(annotationHolder, psiElementRange, OdinSyntaxHighlighter.BUILTIN_FUNCTION);
            } else {
                PsiElement identifierTokenParent = psiElement.getParent();
                if (identifierTokenParent instanceof OdinDeclaredIdentifier declaredIdentifier) {
                    if (declaredIdentifier.getParent() instanceof OdinImportDeclarationStatement) {
                        highlight(annotationHolder, psiElementRange, OdinSyntaxHighlighter.PACKAGE);
                    } else if (declaredIdentifier.getParent() instanceof OdinProcedureDeclarationStatement) {
                        highlight(annotationHolder, psiElementRange, OdinSyntaxHighlighter.PROCEDURE_TYPE);
                    } else if (declaredIdentifier.getParent() instanceof OdinStructDeclarationStatement) {
                        highlight(annotationHolder, psiElementRange, OdinSyntaxHighlighter.STRUCT_TYPE);
                    }
                    // Add other types
                } else if (identifierTokenParent.getParent() instanceof OdinRefExpression refExpression) {
                    handleRefExpression(annotationHolder,
                            psiElement,
                            identifierText,
                            refExpression,
                            identifierTokenParent,
                            psiElementRange);
                } else if (identifierTokenParent.getParent() instanceof OdinQualifiedType qualifiedType) {
                    OdinIdentifier identifier = qualifiedType.getPackageIdentifier();
                    if (identifier.getIdentifierToken() == psiElement) {
                        highlightPackageReference(annotationHolder, identifierText, psiElementRange, identifier);
                    }
                } else if (identifierTokenParent.getParent() instanceof OdinSimpleRefType) {
                    OdinIdentifier identifier = (OdinIdentifier) identifierTokenParent;
                    if (identifier.getReference() != null) {
                        PsiElement resolvedReference = identifier.getReference().resolve();
                        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(resolvedReference, OdinDeclaration.class, false);
                        if (declaration == null) {
                            highlightUnknownReference(identifier.getProject(), annotationHolder, identifierText, psiElementRange);
                        } else if (declaration instanceof OdinStructDeclarationStatement) {
                            highlight(annotationHolder, psiElementRange, OdinSyntaxHighlighter.STRUCT_REF);
                        }
                    }
                }
            }
        }

        highlightEscapeSequences(psiElement, annotationHolder);

        if (psiElement instanceof OdinDirectiveHead tagHead) {
            highlightTagHead(tagHead, annotationHolder);
        }
    }

    private static class OdinAnnotationSessionState {
        Map<PsiElement, OdinRefExpression> refExpressionMap = new HashMap<>();
        Set<OdinRefExpression> aborted = new HashSet<>();
    }

    private static OdinAnnotationSessionState getAnnotationSessionState(AnnotationHolder annotationHolder) {
        Object annotationSessionState = annotationHolder.getCurrentAnnotationSession().getUserData(ANNOTATION_SESSION_STATE);
        if (annotationSessionState == null) {
            OdinAnnotationSessionState newState = new OdinAnnotationSessionState();
            annotationHolder.getCurrentAnnotationSession().putUserData(ANNOTATION_SESSION_STATE, newState);
            return newState;
        }
        return (OdinAnnotationSessionState) annotationSessionState;
    }

    private void handleRefExpression(@NotNull AnnotationHolder annotationHolder,
                                     @NotNull PsiElement psiElement,
                                     String identifierText,
                                     OdinRefExpression refExpression,
                                     PsiElement identifierTokenParent,
                                     TextRange textRange) {
        OdinAnnotationSessionState annotationSessionState = getAnnotationSessionState(annotationHolder);

        // Unfold refExpression
        OdinRefExpression topMostExpression = annotationSessionState.refExpressionMap.get(psiElement);
        if (topMostExpression == null) {
            List<OdinRefExpression> refExpressions = OdinInsightUtils.unfoldRefExpressions(refExpression);
            if (!refExpressions.isEmpty()) {
                topMostExpression = refExpressions.getLast();
                for (OdinRefExpression expression : refExpressions) {
                    annotationSessionState.refExpressionMap.put(expression, topMostExpression);
                }
            }
        }

        if (topMostExpression == null)
            return;

        if (annotationSessionState.aborted.contains(topMostExpression))
            return;

        if (topMostExpression.getIdentifier() == identifierTokenParent) {
            if (identifierText.equals("_"))
                return;
        }

        PsiElement refExpressionParent = refExpression.getParent();

        OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(identifierTokenParent)
                .with(OdinImportService.getInstance(identifierTokenParent.getProject())
                        .getPackagePath(identifierTokenParent));

        OdinSymbol symbol = resolveSymbol(symbolTable, identifierTokenParent);

        if (symbol == null) {
            highlightUnknownReference(identifierTokenParent.getProject(), annotationHolder, identifierText, textRange);
            annotationSessionState.aborted.add(topMostExpression);
            return;
        }

        if (symbol.getPsiType() instanceof OdinPolymorphicType) {
            annotationSessionState.aborted.add(topMostExpression);
            return;
        }

        if(symbol.getPsiType() instanceof OdinPointerType pointerType) {
            if(pointerType.getType() instanceof OdinPolymorphicType) {
                annotationSessionState.aborted.add(topMostExpression);
                return;
            }
        }

        if (symbol.getDeclaredIdentifier() instanceof OdinDeclaredIdentifier declaredIdentifier) {
            if (declaredIdentifier.getDollar() != null)
                return;
        }

        if (symbol.isImplicitlyDeclared())
            return;

        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(symbol.getDeclaredIdentifier(),
                OdinDeclaration.class,
                true);

        if (refExpressionParent instanceof OdinCallExpression) {
            OdinIdentifier identifier = refExpression.getIdentifier();
            if (identifierTokenParent == identifier) {
                if (declaration instanceof OdinProcedureDeclarationStatement) {
                    highlight(annotationHolder, textRange, OdinSyntaxHighlighter.PROCEDURE_CALL);
                } else if (declaration instanceof OdinStructDeclarationStatement) {
                    highlight(annotationHolder, textRange, OdinSyntaxHighlighter.STRUCT_REF);
                } else if (declaration instanceof OdinUnionDeclarationStatement) {
                    highlight(annotationHolder, textRange, OdinSyntaxHighlighter.UNION_REF);
                }
            }
        } else {
            PsiElement lastRefExpression = PsiTreeUtil.findFirstParent(identifierTokenParent,
                    p -> !(p.getParent() instanceof OdinRefExpression));
            OdinIdentifier identifier = PsiTreeUtil.findChildOfType(lastRefExpression, OdinIdentifier.class);
            if (identifier != null && identifier.getIdentifierToken() == psiElement) {
                highlightPackageReference(annotationHolder, identifierText, textRange, identifier);
            }
        }
    }

    @SuppressWarnings("unused")
    private static void highlightUnknownReference(Project project, @NotNull AnnotationHolder annotationHolder, String identifierText, TextRange textRange) {
        OdinSdkConfigPersistentState state = OdinSdkConfigPersistentState.getInstance(project);
        if (state.isHighlightUnknownReferencesEnabled()) {
            annotationHolder
                    .newAnnotation(HighlightSeverity.ERROR, "Unresolved reference '%s'".formatted(identifierText))
                    .range(textRange)
                    .textAttributes(OdinSyntaxHighlighter.BAD_CHARACTER)
                    .create();
            // TODO enable when it's done}
        }
    }

    private void highlightPackageReference(@NotNull AnnotationHolder annotationHolder, String identifierText, TextRange textRange, OdinIdentifier
            identifier) {
        PsiReference reference = identifier.getReference();
        if (reference != null) {
            PsiElement resolveReference = reference.resolve();
            OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(resolveReference, OdinDeclaration.class, false);
            if (odinDeclaration == null) {
                highlightUnknownReference(identifier.getProject(), annotationHolder, identifierText, textRange);
            } else if (odinDeclaration instanceof OdinImportDeclarationStatement) {
                highlight(annotationHolder, textRange, OdinSyntaxHighlighter.PACKAGE);
            }
        }
    }

    private static void highlight(@NotNull AnnotationHolder annotationHolder, TextRange psiElement, TextAttributesKey constant) {
        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(psiElement)
                .textAttributes(constant)
                .create();
    }

    private static void highlightTagHead(OdinDirectiveHead tagHead, @NotNull AnnotationHolder annotationHolder) {

        var matchRange = tagHead.getTextRange();
        highlight(annotationHolder, matchRange, DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
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
            highlight(annotationHolder, matchRange, DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
        }
    }

    private static OdinSymbol resolveSymbol(OdinSymbolTable symbolTable, PsiElement psiElement) {
        if (!(psiElement instanceof OdinIdentifier identifier)) {
            return null;
        }

        PsiReference reference = identifier.getReference();
        if (reference instanceof OdinReference odinReference) {
            try {
                return OdinSymbolTableResolver.findSymbol(odinReference.getElement(), symbolTable);
            } catch (StackOverflowError e) {
                logStackOverFlowError(odinReference.getElement(), LOG);
                return null;
            }
        }

        return null;
    }

}
