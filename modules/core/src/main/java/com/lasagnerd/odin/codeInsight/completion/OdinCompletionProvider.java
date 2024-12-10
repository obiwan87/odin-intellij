package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.symbols.*;
import com.lasagnerd.odin.codeInsight.typeInference.OdinExpectedTypeEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.codeInsight.completion.CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED;
import static com.intellij.codeInsight.completion.PrioritizedLookupElement.withPriority;
import static com.lasagnerd.odin.codeInsight.completion.OdinCompletionContributor.*;

class OdinCompletionProvider extends CompletionProvider<CompletionParameters> {
    private final OdinSymbolFilter symbolFilter;
    private List<LookupElement> sdkPackageCompletions;

    public OdinCompletionProvider(OdinSymbolFilter symbolFilter) {
        this.symbolFilter = symbolFilter;
    }

    public OdinCompletionProvider() {
        this.symbolFilter = new OdinSymbolFilter("NO_FILTER") {
            @Override
            public boolean shouldInclude(OdinSymbol symbol) {
                return true;
            }
        };
    }

    private void addSelectorTypeCompletions(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, @NotNull OdinQualifiedType parentType) {
        OdinContext completionScope = OdinInsightUtils.getReferenceableSymbols(OdinContext.EMPTY, parentType);
        if (completionScope != null) {
            addLookUpElements(result, completionScope.flatten()
                    .getSymbols()
                    .stream()
                    .filter(symbolFilter::shouldInclude)
                    .toList());
        }
    }

    private void addSelectorExpressionCompletions(OdinFile odinFile, @NotNull CompletionResultSet result, OdinRefExpression reference, OdinContext context) {
        OdinExpression expression = reference.getExpression();
        if (expression != null) {
            Project project = expression.getProject();
            TsOdinType refExpressionType = expression.getInferredType();
            OdinContext completionScope = OdinInsightUtils.getReferenceableSymbols(expression);
            if (completionScope != null) {
                Collection<OdinSymbol> visibleSymbols = completionScope.flatten()
                        .getSymbols()
                        .stream()
                        .filter(s -> s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                        .filter(symbolFilter::shouldInclude)
                        .collect(Collectors.toList());

                addLookUpElements(odinFile,
                        null,
                        "",
                        result,
                        visibleSymbols,
                        0,
                        false,
                        (lookupElement, symbol) -> {
                            if (symbol.getSymbolType() == OdinSymbolType.STRUCT_FIELD) {
                                TsOdinType symbolType = OdinInferenceEngine.getSymbolType(project,
                                        symbol,
                                        refExpressionType,
                                        expression);
                                return lookupElement.withTypeText(symbolType.getLabel());
                            }
                            return lookupElement;
                        });

            }
        }
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (!(position.getParent() instanceof OdinIdentifier identifier))
            return;

        VirtualFile sourceFile = OdinImportUtils.getContainingVirtualFile(parameters.getOriginalFile());

        if (!(parameters.getOriginalFile() instanceof OdinFile odinFile))
            return;

        Project project = parameters.getOriginalFile().getProject();

        // Stuff inside arrays, struct init blocks, etc.
        OdinRefExpression topMostRefExpression = OdinInsightUtils.findTopMostRefExpression(identifier);
        if (topMostRefExpression != null && (topMostRefExpression.getParent() instanceof OdinLhs || topMostRefExpression.getParent() instanceof OdinRhs)) {
            OdinCompoundLiteral compoundLiteral = PsiTreeUtil.getParentOfType(topMostRefExpression, OdinCompoundLiteral.class);
            if (compoundLiteral != null) {
                addCompoundLiteralCompletions(result, topMostRefExpression, compoundLiteral);
            }
        }

        PsiElement parent = identifier.getParent();


        // Type assert
        if (parent instanceof OdinType && parent.getParent() instanceof OdinRefExpression refExpression) {
            if (refExpression.getExpression() != null) {
                TsOdinType tsOdinType = refExpression.getExpression().getInferredType();
                if (tsOdinType.baseType(true) instanceof TsOdinUnionType tsOdinUnionType) {
                    addUnionTypeCompletions(result, odinFile, tsOdinUnionType, project, sourceFile, new OdinTypeAssertInsertHandler());
                    return;
                }
            }
        }

        // Case clause
        if (parent instanceof OdinRefExpression refExpression && refExpression.getParent() instanceof OdinCaseClause switchCase) {
            OdinSwitchBlock switchBlock = PsiTreeUtil.getParentOfType(switchCase, OdinSwitchBlock.class);
            if (switchBlock != null) {
                if (switchBlock.getSwitchInClause() != null) {
                    OdinExpression expression = switchBlock.getSwitchInClause().getExpression();
                    TsOdinType tsOdinType = expression.getInferredType();
                    if (tsOdinType.baseType(true) instanceof TsOdinUnionType tsOdinUnionType) {
                        addUnionTypeCompletions(result, odinFile, tsOdinUnionType, project, sourceFile, new OdinTypeAssertInsertHandler());
                    }
                } else if (switchBlock.getExpression() != null) {
                    TsOdinType tsOdinType = switchBlock.getExpression().getInferredType();
                    if (tsOdinType.baseType(true) instanceof TsOdinEnumType tsOdinEnumType) {
                        List<OdinSymbol> enumFields = OdinInsightUtils.getEnumFields((OdinEnumType) tsOdinEnumType.getPsiType());
                        result.startBatch();
                        for (int i = 0; i < enumFields.size(); i++) {
                            OdinSymbol enumField = enumFields.get(i);
                            LookupElementBuilder typeElementSymbol = createTypeElementSymbol(enumField, null, null);
                            result.addElement(withPriority(typeElementSymbol, 10000 + enumFields.size() - i));
                        }
                        result.endBatch();
                    }
                }
            }
        }

        switch (parent) {
            case OdinImplicitSelectorExpression implicitSelectorExpression -> {
                TsOdinType tsOdinType = OdinExpectedTypeEngine
                        .inferExpectedType(
                                OdinContextBuilder.buildFullContext(position),
                                implicitSelectorExpression
                        );

                if (tsOdinType.baseType(true) instanceof TsOdinEnumType tsOdinEnumType) {
                    addImplicitEnumCompletions(result, tsOdinEnumType, project, 0);
                }
            }

            // Qualified types like 'package.<caret>'
            case OdinSimpleRefType ignored when parent.getParent() instanceof OdinQualifiedType qualifiedType ->
                    addSelectorTypeCompletions(parameters, result, qualifiedType);

            // Expressions like 'a.b.c.<caret>'
            case OdinRefExpression refExpression when refExpression.getExpression() != null -> {
                OdinContext context = OdinContextBuilder.buildFullContext(refExpression, parameters
                        .getOriginalFile()
                        .getContainingDirectory()
                        .getVirtualFile()
                        .getPath());

                TsOdinType inferredType = TsOdinBuiltInTypes.UNKNOWN;
                if (refExpression.getExpression() != null) {
                    inferredType = refExpression.getExpression().getInferredType();
                }

                if (!inferredType.isUnknown()) {
                    addSelectorExpressionCompletions(odinFile, result, refExpression, context);
                } else {
                    if (topMostRefExpression != null) {
                        addIdentifierCompletions(parameters,
                                result,
                                topMostRefExpression.getText(),
                                context.flatten());
                    }
                }

            }
            // Identifiers like '<caret>'
            case null, default -> {
                OdinContext context = OdinContextBuilder
                        .buildFullContext(position)
                        .flatten();
                addIdentifierCompletions(parameters, result, position.getText(), context);
            }
        }
    }

    private static void addImplicitEnumCompletions(@NotNull CompletionResultSet result, TsOdinEnumType tsOdinEnumType, Project project, int priority) {
        OdinContext typeElements = OdinInsightUtils.getTypeElements(project, tsOdinEnumType);
        // Sort by definition order
        List<OdinSymbol> symbols = typeElements.getSymbols().stream()
                .sorted(
                        Comparator.comparing(s -> s.getDeclaration().getTextOffset())
                )
                .toList();
        for (int i = 0; i < symbols.size(); i++) {
            OdinSymbol symbol = symbols.get(i);
            LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                    .create(symbol.getName())
                    .withTypeText(tsOdinEnumType.getName())
                    .withPresentableText("." + symbol.getName())
                    .withIcon(getIcon(OdinSymbolType.ENUM_FIELD));

            // Higher for earlier elements
            result.addElement(withPriority(lookupElementBuilder, priority + symbols.size() - i));
        }
    }

    private static void addUnionTypeCompletions(@NotNull CompletionResultSet result,
                                                OdinFile odinFile,
                                                TsOdinUnionType tsOdinUnionType,
                                                Project project,
                                                VirtualFile sourceFile,
                                                OdinTypeAssertInsertHandler insertHandler) {
        result.startBatch();
        List<TsOdinUnionVariant> variants = tsOdinUnionType.getVariants();
        for (int i = 0; i < variants.size(); i++) {
            TsOdinUnionVariant variant = variants.get(i);
            TsOdinType variantType = variant.getType();

            OdinDeclaration declaration = variantType.getDeclaration();
            if (declaration != null) {
                List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver
                        .getSymbols(declaration, variantType.getContext());
                if (!localSymbols.isEmpty()) {
                    OdinSymbol symbol = localSymbols.getFirst();
                    if (symbol.getDeclaration() != null) {
                        VirtualFile targetFile = OdinImportUtils.getContainingVirtualFile(symbol.getDeclaration());
                        OdinImport odinImport = OdinImportUtils.computeRelativeImport(project, sourceFile, targetFile);
                        addLookUpElement(odinFile,
                                odinImport,
                                sourceFile.getParent().getPath(),
                                result,
                                localSymbols.getFirst(),
                                10000 + variants.size() - i,
                                (lookupElementBuilder, __) -> {
                                    LookupElementBuilder bold = lookupElementBuilder.bold();
                                    if (insertHandler != null) {
                                        InsertHandler<LookupElement> currentInsertHandler
                                                = lookupElementBuilder.getInsertHandler();
                                        CombinedInsertHandler newInsertHandler = new CombinedInsertHandler(currentInsertHandler, currentInsertHandler);
                                        return bold.withInsertHandler(newInsertHandler);
                                    }
                                    return bold;
                                }
                        );
                    }
                }
            } else if (variantType instanceof TsOdinBuiltInType builtInType) {
                LookupElementBuilder lookupElement =
                        LookupElementBuilder.create(builtInType.getName()).bold();
                if (insertHandler != null) {
                    lookupElement = lookupElement.withInsertHandler(insertHandler);
                }
                result.addElement(
                        withPriority(lookupElement, 10000 + variants.size() - i)
                );
            }
        }
        result.endBatch();
    }

    private static void addCompoundLiteralCompletions(@NotNull CompletionResultSet result, OdinRefExpression topMostRefExpression, OdinCompoundLiteral compoundLiteral) {
        OdinContext context = OdinContextBuilder.buildFullContext(topMostRefExpression);
        TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(context, compoundLiteral);

        List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, context);
        InsertHandler<LookupElement> insertHandler = new ElementEntryInsertHandler();

        for (OdinSymbol symbol : elementSymbols) {
            LookupElementBuilder element = createTypeElementSymbol(symbol, tsOdinType, insertHandler);

            LookupElement prioritized = withPriority(element, 10000);
            result.addElement(prioritized);
        }
    }

    private static @NotNull LookupElementBuilder createTypeElementSymbol(OdinSymbol symbol,
                                                                         TsOdinType compoundLiteralType,
                                                                         InsertHandler<LookupElement> insertHandler) {
        Icon icon = OdinCompletionContributor.getIcon(symbol.getSymbolType());
        String text = symbol.getSymbolType() == OdinSymbolType.ENUM_FIELD ? "." + symbol.getName() : symbol.getName();

        String typeText = getTypeText(symbol);

        LookupElementBuilder element = LookupElementBuilder
                .create(symbol.getDeclaredIdentifier(), text)
                .withLookupString(symbol.getName())
                .withIcon(icon)
                .withTypeText(typeText);

        if (compoundLiteralType != null)
            element = element
                    .withTailText(" → " + compoundLiteralType.getLabel());

        if (insertHandler != null) {
            element = element.withInsertHandler(insertHandler);
        }
        return element;
    }

    private static @Nullable String getTypeText(OdinSymbol symbol) {
        String typeText = null;
        if (symbol.getPsiType() != null) {
            if (symbol.getSymbolType() == OdinSymbolType.ENUM_FIELD) {
                OdinDeclaration declaration = PsiTreeUtil.getParentOfType(symbol.getPsiType(), OdinDeclaration.class);
                if (declaration != null) {
                    OdinDeclaredIdentifier declaredIdentifier = declaration.getDeclaredIdentifiers().getFirst();
                    typeText = declaredIdentifier.getName();
                }
            }
            if (symbol.getSymbolType() == OdinSymbolType.STRUCT_FIELD) {
                typeText = symbol.getPsiType().getText();
            }
        }
        return typeText;
    }

    private void addIdentifierCompletions(@NotNull CompletionParameters parameters,
                                          @NotNull CompletionResultSet result,
                                          String text,
                                          OdinContext context) {
        String typed = text.replaceAll(DUMMY_IDENTIFIER_TRIMMED + "$", "");

        // Add symbols from all visible stuff in my project
        OdinFile thisOdinFile = (OdinFile) parameters.getOriginalFile();
        VirtualFile thisFile = thisOdinFile.getVirtualFile();
        String thisPackagePath = thisFile.getParent().getPath();
        Project project = thisOdinFile.getProject();

        if (typed.isBlank()) {
            result.restartCompletionOnAnyPrefixChange();
        } else {
            result = result.withPrefixMatcher(typed);
        }

        // Add symbols from local scope
        List<OdinSymbol> symbols = context.getSymbols()
                .stream()
                .filter(symbolFilter::shouldInclude)
                .toList();

        List<OdinSymbol> visibleSymbols = new ArrayList<>();

        for (OdinSymbol symbol : symbols) {
            if (symbol.getDeclaredIdentifier() == null) {
                visibleSymbols.add(symbol);
            } else {
                VirtualFile containingVirtualFile = OdinImportUtils.getContainingVirtualFile(symbol.getDeclaredIdentifier());
                VirtualFile virtualFile = parameters.getOriginalFile().getVirtualFile();
                if (!Objects.equals(containingVirtualFile, virtualFile)) {
                    if (symbol.getSymbolType() == OdinSymbolType.PACKAGE_REFERENCE)
                        continue;
                    visibleSymbols.add(symbol);
                } else {
                    visibleSymbols.add(symbol);
                }
            }
        }
        addLookUpElements(result, visibleSymbols, 2000, (l, s) -> {
            TsOdinType symbolType = OdinInferenceEngine.getSymbolType(project, s, null, parameters.getPosition());
            if (!symbolType.isUnknown()) {
                if (symbolType instanceof TsOdinMetaType metaType && metaType.getRepresentedMetaType() == TsOdinMetaType.MetaType.ALIAS) {
                    return l.withIcon(AllIcons.Nodes.Type);
                }
                if (s.getSymbolType() == OdinSymbolType.VARIABLE || s.getSymbolType() == OdinSymbolType.CONSTANT) {
                    return l.withTypeText(symbolType.getLabel());
                }
            }
            return l;
        });

        // Add symbols from other packages from this source root (the blue folder)
        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
        VirtualFile sourceRoot = projectFileIndex.getSourceRootForFile(thisFile);
        if (!typed.isBlank() || parameters.getInvocationCount() > 1) {
            if (sourceRoot != null) {
                // Recursively walk through all dirs starting from source root
                Map<OdinImport, List<OdinFile>> sourceRootPackages = OdinImportUtils.collectImportablePackages(project,
                        sourceRoot,
                        null,
                        thisFile.getParent().getPath()
                );

                // packages now contains all packages (recursively) under source root
                // add all the symbols to the lookup elements whilst taking into consideration
                // their origin package. Upon accepting a suggestion insert the import statement
                // if not already present
                addPackageCompletions(thisOdinFile, thisPackagePath, sourceRootPackages, result, 1000);
            }

            // Add collection roots completions
            Map<String, Path> collectionPaths = OdinImportUtils.getCollectionPaths(project, thisPackagePath);
            for (Map.Entry<String, Path> entry : collectionPaths.entrySet()) {
                VirtualFile rootDir = VirtualFileManager.getInstance().findFileByNioPath(entry.getValue());
                if (rootDir != null) {
                    Map<OdinImport, List<OdinFile>> collectionPackages = OdinImportUtils.collectImportablePackages(project,
                            rootDir,
                            entry.getKey(),
                            null);
                    addPackageCompletions(thisOdinFile, thisPackagePath, collectionPackages, result, 500);
                }
            }

            // Add sdk packages completions
            OdinSdkService sdkService = OdinSdkService.getInstance(project);
            Map<OdinImport, List<OdinFile>> sdkPackages = sdkService.getSdkPackages();
            if (this.sdkPackageCompletions == null) {
                this.sdkPackageCompletions = addPackageCompletions(thisOdinFile, thisPackagePath, sdkPackages, result, 250);
            } else {
                result.addAllElements(this.sdkPackageCompletions);
            }
        }
    }

    private List<LookupElement> addPackageCompletions(OdinFile thisOdinFile,
                                                      String thisPackagePath,
                                                      Map<OdinImport, List<OdinFile>> packages,
                                                      @NotNull CompletionResultSet result,
                                                      int priority) {
        List<LookupElement> packageCompletions = new ArrayList<>();
        for (Map.Entry<OdinImport, List<OdinFile>> entry : packages.entrySet()) {
            OdinImport odinImport = entry.getKey();

            List<OdinFile> files = entry.getValue();
            for (OdinFile file : files) {
                if (file.getFileScope() == null)
                    continue;
                OdinFileScope fileScope = file.getFileScope();
                OdinContext fileScopeDeclarations = fileScope.getFullContext();

                List<OdinSymbol> visibleSymbols = fileScopeDeclarations
                        .getSymbols(OdinVisibility.PACKAGE_EXPORTED)
                        .stream()
                        .filter(s -> s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                        .filter(symbolFilter::shouldInclude)
                        .toList();

                packageCompletions.addAll(addLookUpElements(thisOdinFile,
                        odinImport,
                        thisPackagePath,
                        result,
                        visibleSymbols,
                        priority));
            }
        }
        return packageCompletions;
    }

    @Getter
    public abstract static class OdinSymbolFilter {
        private final String debugName;

        protected OdinSymbolFilter(String debugName) {
            this.debugName = debugName;
        }

        public abstract boolean shouldInclude(OdinSymbol symbol);

    }

    private static class OdinTypeAssertInsertHandler implements InsertHandler<LookupElement> {
        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
            // when type assert completion is done insert a "." after the right parenthesis
            // after completion it should look like this:
            // expr.(completion).<caret>
            String text = context.getDocument().getText(TextRange.from(context.getTailOffset() + 1, 1));
            if (!text.equals(".")) {
                context.getDocument().insertString(context.getTailOffset() + 1, ".");
            }
            context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() + 2);
            context.commitDocument();
        }
    }

    private static class ElementEntryInsertHandler implements InsertHandler<LookupElement> {
        @Override
        public void handleInsert(@NotNull InsertionContext insertionContext, @NotNull LookupElement item) {
            Document document = insertionContext.getDocument();
            document.insertString(insertionContext.getTailOffset(), " = ");
            insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset());
            PsiDocumentManager.getInstance(insertionContext.getProject()).commitDocument(document);
        }
    }
}
