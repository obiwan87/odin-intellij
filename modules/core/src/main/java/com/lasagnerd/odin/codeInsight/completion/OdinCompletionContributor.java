package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
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
import com.lasagnerd.odin.codeInsight.imports.OdinImportInfo;
import com.lasagnerd.odin.codeInsight.symbols.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdinCompletionContributor extends CompletionContributor {

    public static final PsiElementPattern.@NotNull Capture<PsiElement> REF_EXPRESSION_PATTERN = psiElement(OdinTypes.IDENTIFIER_TOKEN)
            .withSuperParent(2, psiElement(OdinRefExpression.class));

    private static final @NotNull ElementPattern<PsiElement> TYPE_PATTERN = psiElement(OdinTypes.IDENTIFIER_TOKEN)
            .withSuperParent(2, OdinSimpleRefType.class);


    private static final OdinCompletionProvider.OdinSymbolFilter TYPE_FILTER = new OdinCompletionProvider.OdinSymbolFilter("TYPE_FILTER") {
        @Override
        public boolean shouldInclude(OdinSymbol symbol) {
            return  (symbol.getSymbolType().isType() && symbol.getSymbolType() != OdinSymbolType.PROCEDURE && symbol.getSymbolType() != OdinSymbolType.PROCEDURE_OVERLOAD)
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
    }

    public static Icon getIcon(OdinSymbolType symbolType) {
        if (symbolType == null)
            return AllIcons.FileTypes.Unknown;
        return switch (symbolType) {
            case STRUCT -> OdinIcons.Types.Struct;
            case SWIZZLE_FIELD, FIELD -> AllIcons.Nodes.Property;
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
            case POLYMORPHIC_TYPE -> AllIcons.Nodes.Type;
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

    public static void addLookUpElements(@NotNull CompletionResultSet result, Collection<OdinSymbol> symbols) {
        addLookUpElements(null, "", "", result, symbols);
    }

    public static void addLookUpElements(OdinFile sourceFile,
                                          String sourcePackagePath,
                                          String targetPackagePath,
                                          @NotNull CompletionResultSet result,
                                          Collection<OdinSymbol> symbols) {
        String prefix = "";
        if (targetPackagePath != null && !targetPackagePath.isBlank()) {
            // Get last segment of path
            prefix = Path.of(targetPackagePath).getFileName().toString() + ".";
        }

        for (var symbol : symbols) {
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
                                                new OdinInsertImportHandler(sourcePackagePath, targetPackagePath, sourceFile)
                                        )
                                );
                        result.addElement(PrioritizedLookupElement.withPriority(element, 0));
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
                                            .withItemTextItalic(true)
                                            .withIcon(icon)
                                            .withInsertHandler(
                                                    new CombinedInsertHandler(
                                                            new OdinInsertSymbolHandler(symbol.getSymbolType()),
                                                            new OdinInsertImportHandler(sourcePackagePath, targetPackagePath, sourceFile)
                                                    )
                                            );
                                    element = procedureLookupElement(element, declaringProcedure);
                                    result.addElement(
                                            PrioritizedLookupElement.withPriority(element, 0)
                                    );
                                }
                            }
                        }
                    }
                }
                case PACKAGE_REFERENCE -> {
                    OdinImportDeclarationStatement odinDeclaration = PsiTreeUtil.getParentOfType(declaredIdentifier, false, OdinImportDeclarationStatement.class);
                    if (odinDeclaration != null) {
                        OdinImportInfo info = odinDeclaration.getImportInfo();

                        LookupElementBuilder element = LookupElementBuilder.create(info.packageName())
                                .withIcon(AllIcons.Nodes.Package)
                                .withTypeText(info.path());

                        if (info.library() != null) {
                            element = element.withTailText(" -> " + info.library());
                        }

                        result.addElement(PrioritizedLookupElement.withPriority(element, 100));
                    }
                }
                default -> {
                    LookupElementBuilder element = LookupElementBuilder.create(lookupString).withIcon(icon)
                            .withLookupString(unprefixedLookupString)
                            .withInsertHandler(
                                    new CombinedInsertHandler(
                                            new OdinInsertSymbolHandler(symbol.getSymbolType()),
                                            new OdinInsertImportHandler(sourcePackagePath, targetPackagePath, sourceFile)
                                    )
                            );
                    result.addElement(PrioritizedLookupElement.withPriority(element, 0));
                }
            }
        }
    }
}
