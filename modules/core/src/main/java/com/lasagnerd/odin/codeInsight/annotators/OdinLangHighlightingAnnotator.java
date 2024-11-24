package com.lasagnerd.odin.codeInsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Query;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.symbols.*;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinPolymorphicType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.colorSettings.OdinSyntaxTextAttributes;
import com.lasagnerd.odin.lang.OdinParserDefinition;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static com.lasagnerd.odin.codeInsight.annotators.OdinAnnotationUtils.getUserData;
import static com.lasagnerd.odin.colorSettings.OdinSyntaxTextAttributes.ODIN_SHADOWING_VARIABLE;
import static com.lasagnerd.odin.colorSettings.OdinSyntaxTextAttributes.TEXT_ATTRIBUTES_MAP;
import static com.lasagnerd.odin.lang.psi.OdinReference.logStackOverFlowError;


public class OdinLangHighlightingAnnotator implements Annotator {

    public static final String UNRESOLVED_REFERENCE_ERROR_MESSAGE = "Unresolved reference '%s'";
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
    private static final List<String> PREDEFINED_PROCEDURES = List.of(
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
    private static final Key<OdinAnnotationSessionState> ANNOTATION_SESSION_STATE = Key.create("annotationSessionState");
    public static Pattern ESCAPE_SEQUENCES_PATTERN = Pattern.compile("\\\\n|\\\\r|\\\\v|\\\\t|\\\\e|\\\\a|\\\\b|\\\\f|\\\\[0-7]{2}|\\\\x[0-9a-fA-F]{2}|\\\\u[0-9a-fA-F]{4}|\\\\U[0-9a-fA-F]{8}|\\\\\"|\\\\\\\\");

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        OdinProjectSettingsService persistentState = OdinProjectSettingsService.getInstance(annotationHolder.getCurrentAnnotationSession().getFile().getProject());
        if (persistentState != null && !persistentState.isSemanticAnnotatorEnabled())
            return;

        IElementType elementType = PsiUtilCore.getElementType(psiElement);

        TextRange psiElementRange = psiElement.getTextRange();

        if (psiElement instanceof OdinImportDeclarationStatement importDeclarationStatement) {

            OdinImportPath odinImportPath = importDeclarationStatement.getImportPath();
            PsiReference[] references = odinImportPath.getReferences();

            for (PsiReference reference : references) {
                PsiElement resolvedPackageDir = reference.resolve();
                if (resolvedPackageDir == null) {
                    TextRange rangeInElement = reference.getRangeInElement();
                    TextRange textRange = rangeInElement.shiftRight(odinImportPath.getTextRange().getStartOffset());
                    String packagePath = odinImportPath
                            .getText()
                            .substring(rangeInElement.getStartOffset(), rangeInElement.getEndOffset());
                    String type;
                    if (reference instanceof OdinPackageReference) {
                        type = "package path";
                    } else if (reference instanceof OdinCollectionReference) {
                        type = "collection";
                    } else {
                        type = "reference";
                    }
                    highlightError(psiElement.getProject(),
                            annotationHolder,
                            packagePath,
                            textRange,
                            "Unresolved " + type + " '%s'");
                }
            }
        }

        if (elementType == OdinTypes.IDENTIFIER_TOKEN) {

            String identifierText = psiElement.getText();

            if (RESERVED_TYPES.contains(identifierText)) {
                highlight(annotationHolder, psiElementRange, OdinSyntaxTextAttributes.ODIN_BUILTIN_TYPE);
            } else if (PREDEFINED_PROCEDURES.contains(identifierText)) {
                PsiElement identifierTokenParent = psiElement.getParent();
                if (identifierTokenParent.getParent() instanceof OdinCallExpression) {
                    highlight(annotationHolder, psiElementRange, OdinSyntaxTextAttributes.ODIN_BUILTIN_PROC_CALL);
                } else {
                    highlight(annotationHolder, psiElementRange, OdinSyntaxTextAttributes.ODIN_BUILTIN_PROC);
                }
            } else {
                PsiElement identifierTokenParent = psiElement.getParent();
                if (identifierTokenParent instanceof OdinDeclaredIdentifier declaredIdentifier) {
                    handleDeclarations(annotationHolder, declaredIdentifier, psiElementRange);
                    // Add other types
                } else if (identifierTokenParent instanceof OdinAttributeIdentifier) {
                    highlight(annotationHolder, psiElementRange, OdinSyntaxTextAttributes.ODIN_ATTRIBUTE_REF);
                } else if (identifierTokenParent.getParent() instanceof OdinImplicitSelectorExpression) {
                    OdinSymbolTable symbolTable = computeSymbolTable(identifierTokenParent);
                    OdinSymbol symbol = resolveSymbol(symbolTable, identifierTokenParent);
                    if (symbol != null) {
                        highlight(annotationHolder, psiElementRange, OdinSyntaxTextAttributes.ODIN_IMPLICIT_ENUM_FIELD_REF);
                        return;
                    }
                    // TODO: activate when finished with inference engine
                    highlightError(identifierTokenParent.getProject(),
                            annotationHolder,
                            identifierText,
                            psiElementRange,
                            UNRESOLVED_REFERENCE_ERROR_MESSAGE);
                } else if (identifierTokenParent.getParent() instanceof OdinRefExpression refExpression) {
                    handleReferences(annotationHolder,
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
                    handleTypeReferences(annotationHolder,
                            psiElement,
                            identifierTokenParent,
                            identifierText,
                            psiElementRange
                    );
                }
            }
        }

        highlightEscapeSequences(psiElement, annotationHolder);

        if (psiElement instanceof OdinDirectiveIdentifier directiveIdentifier) {
            highlightDirectiveIdentifier(directiveIdentifier, annotationHolder);
        }
    }

    private void handleTypeReferences(AnnotationHolder annotationHolder,
                                      PsiElement element,
                                      PsiElement identifierTokenParent,
                                      String identifierText,
                                      TextRange textRange) {
        OdinSymbolTable symbolTable = computeSymbolTable(identifierTokenParent);
        OdinSymbol symbol = resolveSymbol(symbolTable, identifierTokenParent);
        if (symbol == null) {
            highlightError(element.getProject(), annotationHolder, identifierText, textRange, UNRESOLVED_REFERENCE_ERROR_MESSAGE);
            return;
        }

        if (!OdinInsightUtils.isVisible(identifierTokenParent, symbol)) {
            highlightNotVisible(annotationHolder, identifierText, identifierTokenParent, textRange, symbol);
            return;
        }

        TextAttributesKey textAttribute = TEXT_ATTRIBUTES_MAP.getReferenceTextAttribute(symbol);
        if (textAttribute != null) {
            highlight(annotationHolder, textRange, textAttribute);
        }
    }

    private static void handleDeclarations(@NotNull AnnotationHolder annotationHolder,
                                           OdinDeclaredIdentifier declaredIdentifier,
                                           TextRange psiElementRange) {
        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(declaredIdentifier, OdinDeclaration.class, false);
        if (declaration != null) {
            if (declaration instanceof OdinPackageDeclaration) {
                highlight(annotationHolder, psiElementRange, OdinSyntaxTextAttributes.ODIN_PACKAGE);
                return;
            }
            List<OdinSymbol> symbols = OdinDeclarationSymbolResolver.getSymbols(declaration);
            OdinSymbol symbol = symbols.stream()
                    .filter(s -> s.getName().equals(declaredIdentifier.getName()))
                    .findFirst()
                    .orElse(null);
            if (symbol != null) {
                if (symbol.getSymbolType() == OdinSymbolType.VARIABLE) {
                    if (symbol.isStatic()) {
                        highlight(annotationHolder, psiElementRange, OdinSyntaxTextAttributes.ODIN_STATIC_VARIABLE);
                        return;
                    }

                    OdinSymbolTable odinSymbolTable = OdinSymbolTableResolver.computeSymbolTable(declaration);
                    if (odinSymbolTable.getSymbol(declaredIdentifier.getName()) != null) {
                        highlight(annotationHolder, psiElementRange, ODIN_SHADOWING_VARIABLE);
                        return;
                    }
                }

                // Track reassignment of variables and return parameters
                // A conditional branch analysis is not necessary, because odin always initializes a variable or return parameter with its default value
                if (symbol.getScope() == OdinScope.LOCAL && (symbol.getSymbolType() == OdinSymbolType.VARIABLE || isReturnParameter(declaration))) {

                    OdinAnnotationSessionState annotationSessionState = getAnnotationSessionState(annotationHolder);
                    PsiElement scope;
                    scope = PsiTreeUtil.getParentOfType(declaredIdentifier,
                            OdinStatementList.class,
                            OdinProcedureLiteralType.class,
                            OdinConditionalStatement.class,
                            OdinWhenStatement.class,
                            OdinForStatement.class,
                            OdinSwitchBlock.class);
                    if (scope != null) {
                        HashMap<OdinDeclaredIdentifier, Integer> reassignmentCount = annotationSessionState.reassignmentCount;

                        reassignmentCount.put(declaredIdentifier, 1);

                        Query<PsiReference> search = ReferencesSearch.search(declaredIdentifier, new LocalSearchScope(scope));
                        Collection<PsiReference> references = search.findAll();
                        for (PsiReference reference : references) {
                            PsiElement element = reference.getElement();
                            if (element instanceof OdinIdentifier odinIdentifier) {
                                annotationSessionState.usages.put(odinIdentifier, symbol);
                                if (odinIdentifier.getParent() instanceof OdinRefExpression refExpression) {
                                    if (refExpression.getParent() instanceof OdinLhsExpressions lhsExpressions) {
                                        if (lhsExpressions.getParent() instanceof OdinAssignmentStatement) {
                                            Integer count = reassignmentCount.getOrDefault(declaredIdentifier, 0);
                                            reassignmentCount.put(declaredIdentifier, count + 1);
                                        }
                                    }
                                }
                            }
                        }

                        if (reassignmentCount.getOrDefault(declaredIdentifier, 0) > 1) {
                            highlight(annotationHolder, psiElementRange, OdinSyntaxTextAttributes.ODIN_VAR_REASSIGNMENT);
                            return;
                        }
                    }
                }


                TextAttributesKey textAttribute = TEXT_ATTRIBUTES_MAP.getDeclarationStyle(symbol);
                if (textAttribute != null) {
                    highlight(annotationHolder, psiElementRange, textAttribute);
                }
            }
        }
    }

    private static boolean isReturnParameter(OdinDeclaration declaration) {
        if (declaration instanceof OdinParameterDeclaration) {
            OdinReturnParameters returnParameters = PsiTreeUtil.getParentOfType(declaration, OdinReturnParameters.class);
            return returnParameters != null;
        }
        return false;
    }

    private static class OdinAnnotationSessionState {
        Map<PsiElement, OdinRefExpression> refExpressionMap = new HashMap<>();
        Set<OdinRefExpression> aborted = new HashSet<>();
        Map<OdinIdentifier, OdinSymbol> usages = new HashMap<>();
        HashMap<OdinDeclaredIdentifier, Integer> reassignmentCount = new HashMap<>();
    }

    private static OdinAnnotationSessionState getAnnotationSessionState(AnnotationHolder annotationHolder) {
        return getUserData(annotationHolder.getCurrentAnnotationSession(), ANNOTATION_SESSION_STATE, OdinAnnotationSessionState::new);
    }

    private void handleReferences(@NotNull AnnotationHolder annotationHolder,
                                  @NotNull PsiElement psiElement,
                                  String identifierText,
                                  OdinRefExpression refExpression,
                                  PsiElement identifierTokenParent,
                                  TextRange textRange) {
        OdinAnnotationSessionState annotationSessionState = getAnnotationSessionState(annotationHolder);

        OdinRefExpression topMostExpression = getTopMostExpression(psiElement, refExpression, annotationSessionState);
        if (topMostExpression == null)
            return;

        // If we already found an unknown reference earlier in the reference chain, we skip
        if (annotationSessionState.aborted.contains(topMostExpression))
            return;

        // Ignore any identifier "_"
        if (topMostExpression.getIdentifier() == identifierTokenParent) {
            if (identifierText.equals("_"))
                return;
        }

        PsiElement refExpressionParent = refExpression.getParent();

        OdinSymbolTable symbolTable = computeSymbolTable(identifierTokenParent);


        if (refExpression == topMostExpression) {
            // The first parameter of #config(DEF, val) is not defined in code
            if (isInsideConfigDirective(refExpression)) return;
        }

        OdinSymbol symbol = resolveSymbol(symbolTable, identifierTokenParent);

        // Symbol not found
        if (symbol == null) {

            // If we are being referenced from a polymorphic type, do not show any errors
            if (refExpression.getExpression() != null) {
                TsOdinType type = refExpression.getExpression().getInferredType(symbolTable);
                TsOdinType referenceableType = OdinInsightUtils.getReferenceableType(type);
                if (referenceableType instanceof TsOdinPolymorphicType) {
                    annotationSessionState.aborted.add(topMostExpression);
                    return;
                }
            }

            // Otherwise, annotate as error
            highlightError(identifierTokenParent.getProject(), annotationHolder, identifierText, textRange, UNRESOLVED_REFERENCE_ERROR_MESSAGE);
            annotationSessionState.aborted.add(topMostExpression);
            return;
        }

        if (!OdinInsightUtils.isVisible(identifierTokenParent, symbol)) {
            highlightNotVisible(annotationHolder,
                    identifierText,
                    identifierTokenParent,
                    textRange,
                    symbol);
            return;
        }

        if (symbol.getDeclaredIdentifier() instanceof OdinDeclaredIdentifier declaredIdentifier) {
            Integer count = annotationSessionState.reassignmentCount.getOrDefault(declaredIdentifier, 0);
            if (count > 1) {
                if (symbol.getScope() == OdinScope.LOCAL) {
                    if (symbol.getSymbolType() == OdinSymbolType.PARAMETER || symbol.getSymbolType() == OdinSymbolType.VARIABLE) {
                        highlight(annotationHolder, textRange, OdinSyntaxTextAttributes.ODIN_VAR_REASSIGNMENT_REF);
                        return;
                    }
                }
            }
        }

        if (symbol.getSymbolType() == OdinSymbolType.PARAMETER) {
            if (symbol.isContext()) {
                highlight(annotationHolder, textRange, OdinSyntaxTextAttributes.ODIN_CONTEXT_PARAM_REF);
                return;
            }
        }

        if (symbol.getSymbolType() == OdinSymbolType.POLYMORPHIC_TYPE) {
            highlight(annotationHolder, textRange, OdinSyntaxTextAttributes.ODIN_POLY_PARAMETER_REF);
            return;
        }

        if (symbol.getSymbolType() == OdinSymbolType.VARIABLE) {
            if (OdinAttributeUtils.containsAttribute(symbol.getAttributes(), "static")) {
                highlight(annotationHolder, textRange, OdinSyntaxTextAttributes.ODIN_STATIC_VARIABLE);
                return;
            }
            if (symbol.isBuiltin()) {
                highlight(annotationHolder, textRange, OdinSyntaxTextAttributes.ODIN_BUILTIN_VAR);
                return;
            }

            if (symbolTable.isShadowing(symbol.getName())) {
                highlight(annotationHolder, textRange, OdinSyntaxTextAttributes.ODIN_SHADOWING_VARIABLE_REF);
                return;
            }
        }

        if (symbol.getSymbolType() == OdinSymbolType.CONSTANT) {
            if (symbol.isBuiltin()) {
                highlight(annotationHolder, textRange, OdinSyntaxTextAttributes.ODIN_BUILTIN_CONSTANT);
                return;
            }
        }

        // Check if we have a call expression at hand
        if (refExpressionParent instanceof OdinCallExpression) {
            TextAttributesKey callTextAttribute = TEXT_ATTRIBUTES_MAP.getCallTextAttribute(symbol);
            if (callTextAttribute != null) {
                highlight(annotationHolder, textRange, callTextAttribute);
            }
            return;
        }

        // Check if we have a reference text attribute for this symbol
        TextAttributesKey referenceTextAttribute = TEXT_ATTRIBUTES_MAP.getReferenceTextAttribute(symbol);
        if (referenceTextAttribute != null) {
            highlight(annotationHolder, textRange, referenceTextAttribute);
            return;
        }

        PsiElement lastRefExpression = PsiTreeUtil.findFirstParent(identifierTokenParent,
                p -> !(p.getParent() instanceof OdinRefExpression));
        OdinIdentifier identifier = PsiTreeUtil.findChildOfType(lastRefExpression, OdinIdentifier.class);
        if (identifier != null && identifier.getIdentifierToken() == psiElement) {
            highlightPackageReference(annotationHolder, identifierText, textRange, identifier);
        }
    }

    private static void highlightNotVisible(@NotNull AnnotationHolder annotationHolder, String identifierText, PsiElement identifierTokenParent, TextRange textRange, OdinSymbol symbol) {
        if (symbol.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE) {
            highlightError(identifierTokenParent.getProject(),
                    annotationHolder,
                    identifierText,
                    textRange,
                    UNRESOLVED_REFERENCE_ERROR_MESSAGE);
            return;
        }
        highlightError(identifierTokenParent.getProject(), annotationHolder, identifierText, textRange,
                "'%s' is not visible");
    }

    private static OdinSymbolTable computeSymbolTable(PsiElement identifierTokenParent) {
        return OdinSymbolTableResolver.computeSymbolTable(identifierTokenParent)
                .with(OdinImportService.getInstance(identifierTokenParent.getProject())
                        .getPackagePath(identifierTokenParent));
    }

    private static @Nullable OdinRefExpression getTopMostExpression(@NotNull PsiElement psiElement, OdinRefExpression refExpression, OdinAnnotationSessionState annotationSessionState) {
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
        return topMostExpression;
    }

    private static boolean isInsideConfigDirective(OdinRefExpression refExpression) {
        if (refExpression.getParent() instanceof OdinArgument argument) {
            OdinCallExpression callExpression = PsiTreeUtil.getParentOfType(argument, OdinCallExpression.class);
            if (callExpression != null && callExpression.getExpression() instanceof OdinDirectiveExpression directiveExpression) {
                if (directiveExpression.getText().equals("#config")) {
                    callExpression.getArgumentList();
                    if (!callExpression.getArgumentList().isEmpty()) {
                        return callExpression.getArgumentList().getFirst() == argument;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private static void highlightError(Project project,
                                       @NotNull AnnotationHolder annotationHolder,
                                       String identifierText,
                                       TextRange textRange,
                                       String errorMessage) {
        OdinProjectSettingsService state = OdinProjectSettingsService.getInstance(project);
        if (state.isHighlightUnknownReferencesEnabled()) {
            annotationHolder
                    .newAnnotation(HighlightSeverity.ERROR, errorMessage.formatted(identifierText))
                    .range(textRange)
                    .textAttributes(OdinSyntaxTextAttributes.ODIN_UNKNOWN_REF)
                    .create();
        }
    }

    private void highlightPackageReference(@NotNull AnnotationHolder annotationHolder, String identifierText, TextRange textRange, OdinIdentifier
            identifier) {
        PsiReference reference = identifier.getReference();
        if (reference != null) {
            PsiElement resolveReference = reference.resolve();
            if (resolveReference instanceof PsiDirectory) {
                highlight(annotationHolder, textRange, OdinSyntaxTextAttributes.ODIN_PACKAGE_REF);
            } else {
                OdinDeclaration odinDeclaration = PsiTreeUtil.getParentOfType(resolveReference, OdinDeclaration.class, false);
                if (odinDeclaration == null) {
                    highlightError(identifier.getProject(), annotationHolder, identifierText, textRange, UNRESOLVED_REFERENCE_ERROR_MESSAGE);
                } else if (odinDeclaration instanceof OdinImportDeclarationStatement) {
                    highlight(annotationHolder, textRange, OdinSyntaxTextAttributes.ODIN_PACKAGE_REF);
                }
            }
        }
    }

    private static void highlight(@NotNull AnnotationHolder annotationHolder, TextRange textRange, TextAttributesKey constant) {
        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(textRange)
                .textAttributes(constant)
                .create();
    }

    private static void highlightDirectiveIdentifier(OdinDirectiveIdentifier tagHead, @NotNull AnnotationHolder annotationHolder) {

        var matchRange = tagHead.getTextRange();
        highlight(annotationHolder, matchRange, OdinSyntaxTextAttributes.ODIN_DIRECTIVE_REF);
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
            highlight(annotationHolder, matchRange, OdinSyntaxTextAttributes.ODIN_VALID_STRING_ESCAPE);
        }
    }

    private static OdinSymbol resolveSymbol(OdinSymbolTable symbolTable, PsiElement psiElement) {
        if (!(psiElement instanceof OdinIdentifier identifier)) {
            return null;
        }

        try {
            return identifier.getReference().getSymbol();
        } catch (StackOverflowError e) {
            logStackOverFlowError(identifier, LOG);
            return null;
        }

    }

}

