package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdinCompletionContributor extends CompletionContributor {

    public static final PsiElementPattern.@NotNull Capture<PsiElement> REF_EXPRESSION_PATTERN = psiElement(OdinTypes.IDENTIFIER_TOKEN)
            .withSuperParent(2, psiElement(OdinRefExpression.class));

    public static final PsiElementPattern.@NotNull Capture<PsiElement> IMPLICIT_SELECTOR_EXPRESSION_PATTERN = psiElement(OdinTypes.IDENTIFIER_TOKEN)
            .withSuperParent(2, psiElement(OdinImplicitSelectorExpression.class));
    private static final @NotNull ElementPattern<PsiElement> TYPE_PATTERN = psiElement(OdinTypes.IDENTIFIER_TOKEN)
            .withSuperParent(2, OdinSimpleRefType.class);

    private static final ElementPattern<PsiElement> IMPORT_PATH = psiElement(OdinTypes.DQ_STRING_LITERAL).withParent(OdinImportPath.class);

    private static final OdinCompletionProvider.OdinSymbolFilter TYPE_FILTER = new OdinCompletionProvider.OdinSymbolFilter("TYPE_FILTER") {
        @Override
        public boolean shouldInclude(OdinSymbol symbol) {
            return (symbol.getSymbolType().isType() && symbol.getSymbolType() != OdinSymbolType.PROCEDURE && symbol.getSymbolType() != OdinSymbolType.PROCEDURE_OVERLOAD)
                    || symbol.getSymbolType() == OdinSymbolType.CONSTANT;
        }
    };


    public OdinCompletionContributor() {
        // REFERENCE completion
        extend(CompletionType.BASIC,
                REF_EXPRESSION_PATTERN,
                new OdinCompletionProvider()
        );

        extend(CompletionType.BASIC, TYPE_PATTERN, new OdinCompletionProvider(TYPE_FILTER));
        extend(CompletionType.BASIC, IMPLICIT_SELECTOR_EXPRESSION_PATTERN, new OdinCompletionProvider());
        extend(CompletionType.BASIC, IMPORT_PATH, new OdinImportPathCompletionProvider());
    }

    public static Icon getIcon(OdinSymbolType symbolType) {
        if (symbolType == null)
            return AllIcons.FileTypes.Unknown;
        return switch (symbolType) {
            case STRUCT -> OdinIcons.Types.Struct;
            case SWIZZLE_FIELD, STRUCT_FIELD, SOA_FIELD, BIT_FIELD_FIELD, ALLOCATOR_FIELD -> AllIcons.Nodes.Property;
            case TYPE_ALIAS, POLYMORPHIC_TYPE -> AllIcons.Nodes.Type;
            case BIT_FIELD, LABEL, FOREIGN_IMPORT, BIT_SET, BUILTIN_TYPE -> null;
            case ENUM_FIELD -> AllIcons.Nodes.Field;
            case ENUM -> AllIcons.Nodes.Enum;
            case UNION -> OdinIcons.Types.Union;
            case PROCEDURE, PROCEDURE_OVERLOAD -> AllIcons.Nodes.Function;
            case VARIABLE -> AllIcons.Nodes.Variable;
            case CONSTANT -> AllIcons.Nodes.Constant;
            case PACKAGE_REFERENCE -> AllIcons.Nodes.Package;
            case PARAMETER -> AllIcons.Nodes.Parameter;
            case UNKNOWN -> AllIcons.FileTypes.Unknown;
        };
    }

    @NotNull
    private static LookupElementBuilder procedureLookupElement(LookupElementBuilder element, @NotNull OdinProcedureType procedureType) {
        var params = procedureType.getParamEntryList();
        StringBuilder tailText = new StringBuilder("(");
        String paramList = params.stream().map(PsiElement::getText).collect(Collectors.joining(", "));
        tailText.append(paramList);
        tailText.append(")");
        element = element.withTailText(tailText.toString());

        OdinReturnParameters returnType = procedureType.getReturnParameters();
        if (returnType != null) {
            element = element.withTypeText(returnType.getText());
        }
        return element;
    }

    public static List<LookupElement> addLookUpElement(OdinFile sourceFile,
                                                       OdinImport odinImport,
                                                       String sourcePackagePath,
                                                       @NotNull CompletionResultSet result,
                                                       OdinSymbol symbol,
                                                       int priority) {
        return addLookUpElements(sourceFile,
                odinImport,
                sourcePackagePath,
                result,
                Collections.singleton(symbol),
                priority,
                false,
                Function.identity());
    }

    public static List<LookupElement> addLookUpElement(OdinFile sourceFile,
                                                       OdinImport odinImport,
                                                       String sourcePackagePath,
                                                       @NotNull CompletionResultSet result,
                                                       OdinSymbol symbol,
                                                       int priority,
                                                       Function<LookupElementBuilder, LookupElementBuilder> transformer) {
        return addLookUpElements(sourceFile,
                odinImport,
                sourcePackagePath,
                result,
                Collections.singleton(symbol),
                priority,
                false,
                transformer);
    }


    public static List<LookupElement> addLookUpElements(@NotNull CompletionResultSet result, Collection<OdinSymbol> symbols) {
        return addLookUpElements(result, symbols, 0);
    }

    public static List<LookupElement> addLookUpElements(@NotNull CompletionResultSet result, Collection<OdinSymbol> symbols, int priority) {
        return addLookUpElements(null,
                null,
                "",
                result,
                symbols,
                priority);
    }

    public static List<LookupElement> addLookUpElements(OdinFile sourceFile,
                                                        OdinImport odinImport,
                                                        String sourcePackagePath,
                                                        @NotNull CompletionResultSet result,
                                                        Collection<OdinSymbol> symbols,
                                                        int priority
    ) {
        return addLookUpElements(sourceFile,
                odinImport,
                sourcePackagePath,
                result,
                symbols,
                priority,
                false,
                Function.identity());
    }

    public static List<LookupElement> addLookUpElements(OdinFile sourceFile,
                                                        OdinImport odinImport,
                                                        String sourcePackagePath,
                                                        @NotNull CompletionResultSet result,
                                                        Collection<OdinSymbol> symbols,
                                                        int priority,
                                                        boolean batchMode,
                                                        @NotNull Function<LookupElementBuilder, LookupElementBuilder> transformer
    ) {
        List<LookupElement> lookupElements = new ArrayList<>();
        String prefix = "";
        if (odinImport != null) {
            // Get last segment of path
            if (odinImport.alias() == null) {
                prefix = odinImport.packageName() + ".";
            } else {
                prefix = odinImport.alias() + ".";
            }
        }

        if (batchMode) {
            result.startBatch();
        }
        for (var symbol : symbols) {
            LookupElement lookupElement = null;
            Icon icon = getIcon(symbol.getSymbolType());
            @Nullable PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();

            final String unprefixedLookupString = symbol.getName();
            final String lookupString = prefix + symbol.getName();

            switch (symbol.getSymbolType()) {
                case PROCEDURE -> {
                    if (declaredIdentifier == null) break;

                    LookupElementBuilder element = LookupElementBuilder
                            .create(lookupString)
                            .withLookupString(unprefixedLookupString)
                            .withIcon(icon);


                    OdinProcedureType procedureType = OdinInsightUtils.getProcedureType(declaredIdentifier);

                    if (procedureType != null) {
                        element = procedureLookupElement(element, procedureType)
                                .withInsertHandler(
                                        new CombinedInsertHandler(
                                                new OdinInsertSymbolHandler(symbol.getSymbolType()),
                                                new OdinInsertImportHandler(odinImport, sourcePackagePath, sourceFile)
                                        )
                                );
                        lookupElement = PrioritizedLookupElement
                                .withPriority(transformer.apply(element), priority);

                    }
                }
                case PROCEDURE_OVERLOAD -> {
                    OdinProcedureOverloadType procedureOverloadType = OdinInsightUtils
                            .getDeclaredType(declaredIdentifier, OdinProcedureOverloadType.class);
                    if (procedureOverloadType == null)
                        break;

                    for (var procedureRef : procedureOverloadType.getProcedureRefList()) {
                        OdinIdentifier odinIdentifier = OdinPsiUtil.getIdentifier(procedureRef);

                        if (odinIdentifier == null)
                            continue;

                        PsiReference resolvedReference = odinIdentifier.getReference();

                        if (resolvedReference != null) {
                            PsiElement resolved = resolvedReference.resolve();
                            if (resolved instanceof OdinDeclaredIdentifier) {
                                OdinProcedureType declaringProcedure = OdinInsightUtils.getProcedureType(resolved);
                                if (declaringProcedure != null) {
                                    LookupElementBuilder element = LookupElementBuilder
                                            .create(resolved, lookupString)
                                            .withLookupString(unprefixedLookupString)
                                            .withItemTextItalic(true)
                                            .withIcon(icon)
                                            .withInsertHandler(
                                                    new CombinedInsertHandler(
                                                            new OdinInsertSymbolHandler(symbol.getSymbolType()),
                                                            new OdinInsertImportHandler(odinImport, sourcePackagePath, sourceFile)
                                                    )
                                            );
                                    element = transformer.apply(procedureLookupElement(element, declaringProcedure));
                                    lookupElement = PrioritizedLookupElement.withPriority(element, priority);
                                }
                            }
                        }
                    }
                }
                case PACKAGE_REFERENCE -> {
                    OdinImportDeclarationStatement odinDeclaration = PsiTreeUtil.getParentOfType(declaredIdentifier, false, OdinImportDeclarationStatement.class);
                    if (odinDeclaration != null) {
                        OdinImport info = odinDeclaration.getImportInfo();

                        LookupElementBuilder element = LookupElementBuilder
                                .create(info.packageName())
                                .withIcon(AllIcons.Nodes.Package)
                                .withTypeText(info.path());

                        if (info.collection() != null) {
                            element = element.withTailText(" -> " + info.collection());
                        }

                        lookupElement = PrioritizedLookupElement.withPriority(transformer.apply(element), priority + 100);
                    }
                }
                default -> {
                    LookupElementBuilder element = LookupElementBuilder.create(lookupString)
                            .withIcon(icon)
                            .withLookupString(unprefixedLookupString)
                            .withInsertHandler(
                                    new CombinedInsertHandler(
                                            new OdinInsertSymbolHandler(symbol.getSymbolType()),
                                            new OdinInsertImportHandler(odinImport, sourcePackagePath, sourceFile)
                                    )
                            );
                    lookupElement = PrioritizedLookupElement.withPriority(transformer.apply(element), priority);
                }
            }

            if (lookupElement != null) {
                result.addElement(lookupElement);
                lookupElements.add(lookupElement);
            }
        }

        if (batchMode) {
            result.endBatch();
        }
        return lookupElements;
    }
}
