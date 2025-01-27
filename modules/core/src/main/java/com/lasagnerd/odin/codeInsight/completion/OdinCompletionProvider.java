package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.dataflow.OdinLattice;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService;
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinReferenceResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolType;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinFullSymbolTableBuilder;
import com.lasagnerd.odin.codeInsight.typeInference.OdinExpectedTypeEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.OdinSyntaxHighlighter;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.lang.stubs.indexes.OdinAllPublicNamesIndex;
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

@SuppressWarnings("SameParameterValue")
class OdinCompletionProvider extends CompletionProvider<CompletionParameters> {
    private final OdinSymbolFilter symbolFilter;

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

    private static void addUnionTypeCompletions(@NotNull CompletionResultSet result,
                                                OdinFile odinFile,
                                                TsOdinUnionType tsOdinUnionType,
                                                Project project,
                                                VirtualFile sourceFile,
                                                OdinTypeAssertInsertHandler insertHandler, CompletionParameters parameters) {
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
                        VirtualFile targetFile = OdinInsightUtils.getContainingVirtualFile(symbol.getDeclaration());
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
                                }, parameters
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

    private static void addImplicitEnumCompletions(@NotNull CompletionResultSet result,
                                                   TsOdinEnumType tsOdinEnumType,
                                                   Project project,
                                                   int priority) {
        // TODO(lasagnerd) does context need any knowledge information?
        OdinSymbolTable typeElements = OdinInsightUtils.getTypeElements(new OdinContext(), project, tsOdinEnumType);
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

    private void addSelectorTypeCompletions(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, @NotNull OdinQualifiedType parentType) {
        OdinSymbolTable completionScope = OdinInsightUtils.getReferenceableSymbols(new OdinContext(), parentType);
        if (completionScope != null) {
            addLookUpElements(result, completionScope.flatten()
                    .getSymbols()
                    .stream()
                    .filter(symbolFilter::shouldInclude)
                    .toList(), parameters);
        }
    }

    private void addSelectorExpressionCompletions(OdinFile odinFile, @NotNull CompletionResultSet result, OdinRefExpression reference, CompletionParameters parameters) {
        OdinExpression expression = reference.getExpression();
        if (expression != null) {
            Project project = expression.getProject();
            OdinContext context = new OdinContext();
            TsOdinType refExpressionType = expression.getInferredType(context);
            OdinSymbolTable completionSymbols = OdinInsightUtils.getReferenceableSymbols(context, expression);
            if (completionSymbols != null) {
                Collection<OdinSymbol> visibleSymbols = completionSymbols.flatten()
                        .getSymbols()
                        .stream()
                        .filter(s -> s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                        .filter(symbolFilter::shouldInclude)
                        .collect(Collectors.toList());

                addLookUpElements(parameters, odinFile,
                        null,
                        "",
                        result,
                        visibleSymbols,
                        0,
                        false,
                        (lookupElement, symbol) -> {
                            if (symbol.getSymbolType() == OdinSymbolType.STRUCT_FIELD) {
                                TsOdinType symbolType = OdinInferenceEngine.getSymbolType(
                                        new OdinContext(),
                                        project,
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

    private static @NotNull LookupElementBuilder createTypeElementSymbol(OdinSymbol symbol,
                                                                         TsOdinType compoundLiteralType,
                                                                         InsertHandler<LookupElement> insertHandler) {
        Icon icon = getIcon(symbol.getSymbolType());
        String text = symbol.getSymbolType() == OdinSymbolType.ENUM_FIELD ? "." + symbol.getName() : symbol.getName();

        String typeText = getTypeText(symbol);

        PsiElement declaredIdentifier = symbol.getDeclaredIdentifier();
        declaredIdentifier = declaredIdentifier == null ? symbol.getDeclaration() : null;

        LookupElementBuilder builder;

        if (declaredIdentifier != null) {
            builder = LookupElementBuilder
                    .create(declaredIdentifier, text);
        } else {
            builder = LookupElementBuilder.create(symbol.getName());
        }

        builder = builder
                .withLookupString(symbol.getName())
                .withIcon(icon)
                .withTypeText(typeText);

        if (compoundLiteralType != null)
            builder = builder
                    .withTailText(" → " + compoundLiteralType.getLabel());

        if (insertHandler != null) {
            builder = builder.withInsertHandler(insertHandler);
        }
        return builder;
    }

    private static void addCompoundLiteralCompletions(@NotNull CompletionResultSet result, OdinCompoundLiteral compoundLiteral, OdinContext context) {
        TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(context, compoundLiteral);

        List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, context);
        InsertHandler<LookupElement> insertHandler = new ElementEntryInsertHandler();

        for (OdinSymbol symbol : elementSymbols) {
            LookupElementBuilder element = createTypeElementSymbol(symbol, tsOdinType, insertHandler);

            LookupElement prioritized = withPriority(element, 10000);
            result.addElement(prioritized);
        }
    }

    private static @NotNull Set<String> collectAllNames(PrefixMatcher matcher, OdinFile thisOdinFile) {
        Set<String> aliases = OdinImportUtils.getImports(thisOdinFile.getFileScope()).stream()
                .filter(i -> i.alias() != null)
                .map(OdinImport::canonicalName)
                .collect(Collectors.toSet());

        String prefix = matcher.getPrefix();
        boolean prefixIsEmpty = prefix.isEmpty();

        Set<String> allNames = new HashSet<>();
        CancellableCollectProcessor<String> processor = new CancellableCollectProcessor<>(allNames) {
            @Override
            protected boolean accept(String s) {
                return prefixIsEmpty || matcher.prefixMatches(s) || matcher.prefixMatches(substringAfter(s, '.')) || aliases.contains(substringBefore(s, '.'));
            }
        };

        // TODO(lasagnerd) add production filter and scope
        StubIndex.getInstance().processAllKeys(
                OdinAllPublicNamesIndex.ALL_PUBLIC_NAMES,
                processor,
                GlobalSearchScope.allScope(thisOdinFile.getProject()),
                null
        );

        List<String> sorted = ContainerUtil.sorted(allNames, String.CASE_INSENSITIVE_ORDER);
        ProgressManager.checkCanceled();

        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String name : sorted) {
            ProgressManager.checkCanceled();
            if (matcher.isStartMatch(name)) {
                result.add(name);
            }
        }
        result.addAll(sorted);
        return result;
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

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet result) {

        PsiElement position = parameters.getPosition();
        if (!(position.getParent() instanceof OdinIdentifier identifier))
            return;

        int offset = parameters.getOffset();
        if (offset > 0) {
            PsiElement element = parameters.getOriginalFile().findElementAt(offset - 1);
            if (element != null) {
                IElementType elementType = element.getNode().getElementType();
                if (OdinSyntaxHighlighter.NUMERIC_LITERALS.contains(elementType) || OdinSyntaxHighlighter.STRING_LITERAL_ELEMENTS.contains(elementType)) {
                    return;
                }
            }
        }

        OdinLattice lattice = OdinReferenceResolver.computeExplicitKnowledge(new OdinContext(), identifier);
        OdinContext context = lattice.toContext();
        context.setUseKnowledge(false);

        VirtualFile sourceFile = OdinInsightUtils.getContainingVirtualFile(parameters.getOriginalFile());

        if (!(parameters.getOriginalFile() instanceof OdinFile odinFile))
            return;

        Project project = parameters.getOriginalFile().getProject();

        // Stuff inside arrays, struct init blocks, etc.
        OdinRefExpression topMostRefExpression = OdinInsightUtils.findTopMostRefExpression(identifier);
        if (topMostRefExpression != null && (topMostRefExpression.getParent() instanceof OdinLhs || topMostRefExpression.getParent() instanceof OdinRhs)) {
            OdinCompoundLiteral compoundLiteral = PsiTreeUtil.getParentOfType(topMostRefExpression, OdinCompoundLiteral.class);
            if (compoundLiteral != null) {
                addCompoundLiteralCompletions(result, compoundLiteral, context);
            }
        }

        PsiElement parent = identifier.getParent();


        // Type assert
        if (parent instanceof OdinType && parent.getParent() instanceof OdinRefExpression refExpression) {
            if (refExpression.getExpression() != null) {
                TsOdinType tsOdinType = refExpression.getExpression().getInferredType(context);
                if (tsOdinType.baseType(true) instanceof TsOdinUnionType tsOdinUnionType) {
                    addUnionTypeCompletions(result, odinFile, tsOdinUnionType, project, sourceFile, new OdinTypeAssertInsertHandler(), parameters);
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
                    TsOdinType tsOdinType = expression.getInferredType(context);
                    if (tsOdinType.baseType(true) instanceof TsOdinUnionType tsOdinUnionType) {
                        addUnionTypeCompletions(result, odinFile, tsOdinUnionType, project, sourceFile, new OdinTypeAssertInsertHandler(), parameters);
                    }
                } else if (switchBlock.getExpression() != null) {
                    TsOdinType tsOdinType = switchBlock.getExpression().getInferredType(context);
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
                                new OdinContext(),
                                implicitSelectorExpression
                        );

                if (tsOdinType.baseType(true) instanceof TsOdinEnumType tsOdinEnumType) {
                    addImplicitEnumCompletions(result, tsOdinEnumType, project, 0);
                }
            }

            // Qualified types like 'package.<caret>'
            case OdinSimpleRefType ignored when parent.getParent() instanceof OdinQualifiedType qualifiedType ->
                    addSelectorTypeCompletions(parameters, result, qualifiedType);

            case OdinSimpleRefType ignored -> addIdentifierCompletionsWithStubs(parameters, position.getText(), odinFile, result);

            // Expressions like 'a.b.c.<caret>'
            case OdinRefExpression refExpression when refExpression.getExpression() != null -> {
                TsOdinType inferredType = TsOdinBuiltInTypes.UNKNOWN;
                OdinSymbol referencedSymbol = null;

                if (topMostRefExpression != null && topMostRefExpression.getIdentifier() != null) {
                    referencedSymbol = topMostRefExpression.getIdentifier().getReferencedSymbol();
                }

                if (refExpression.getExpression() != null) {
                    inferredType = refExpression.getExpression().getInferredType(context);
                }

                if (!inferredType.isUnknown()) {
                    addSelectorExpressionCompletions(odinFile, result, refExpression, parameters);
                } else if (referencedSymbol == null) {
                    if (topMostRefExpression != null) {
                        addIdentifierCompletionsWithStubs(
                                parameters,
                                topMostRefExpression.getText(),
                                odinFile,
                                result);
                    }
                }

            }
            // Identifiers like '<caret>'
            case OdinRefExpression refExpression when refExpression.getExpression() == null -> addIdentifierCompletionsWithStubs(
                    parameters,
                    position.getText(),
                    odinFile,
                    result);
            default -> {
                // Do not add completions if we are not inside a ref-expression
            }
        }
    }

    private void addIdentifierCompletionsWithStubs(
            CompletionParameters parameters,
            String text,
            OdinFile thisOdinFile,
            @NotNull CompletionResultSet completionResultSet) {

        OdinFullSymbolTableBuilder builder = new OdinFullSymbolTableBuilder(new OdinContext(), parameters.getPosition());
        String typed = text.replaceAll(DUMMY_IDENTIFIER_TRIMMED + "$", "");
        if (typed.isBlank()) {
            completionResultSet.restartCompletionOnAnyPrefixChange();
        } else {
            completionResultSet = completionResultSet.withPrefixMatcher(typed);
        }

        OdinSymbolTable currentSymbolTable = builder.build();
        List<OdinSymbol> localSymbols = new ArrayList<>();
        while (currentSymbolTable != null) {
            if (currentSymbolTable.getScopeBlock() == null)
                break;
            localSymbols.addAll(currentSymbolTable.getSymbols().stream().filter(symbolFilter::shouldInclude).toList());
            currentSymbolTable = currentSymbolTable.getParentSymbolTable();
        }
        localSymbols = localSymbols.reversed();

        addLookUpElements(completionResultSet, localSymbols, 30, parameters);
        List<OdinSymbol> builtInSymbols = OdinSdkService
                .getInstance(thisOdinFile.getProject())
                .getBuiltInSymbols();

        addLookUpElements(completionResultSet, builtInSymbols.stream()
                .filter(symbolFilter::shouldInclude)
                .filter(s -> s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                .toList(), 20, parameters);

        Project project = thisOdinFile.getProject();
        GlobalSearchScope globalSearchScope;

        if (typed.contains(".") || parameters.getInvocationCount() > 1) {
            globalSearchScope = GlobalSearchScope.allScope(project);
        } else {
            globalSearchScope = GlobalSearchScope.projectScope(project);
        }
        Set<String> allNamesSorted = collectAllNames(completionResultSet.getPrefixMatcher(), thisOdinFile);
        VirtualFile thisVirtualFile = thisOdinFile.getVirtualFile();
        VirtualFile thisParentPath = thisVirtualFile.getParent();

        String thisPackagePath = thisParentPath.getPath();
        String sourceFilePath = thisVirtualFile.getPath();

        Map<Path, OdinImport> importPathMap = OdinImportUtils.getImportPathMap(thisOdinFile);

        CompletionsProcessor processor = new CompletionsProcessor(thisOdinFile, this.symbolFilter, thisPackagePath, parameters);

        for (String name : allNamesSorted) {
            if (name.equals("_"))
                continue;

            Collection<OdinDeclaration> declarations = StubIndex.getElements(OdinAllPublicNamesIndex.ALL_PUBLIC_NAMES, name, project, globalSearchScope, OdinDeclaration.class);

            for (OdinDeclaration declaration : declarations) {
                if (OdinSdkService.isInBuiltinOdinFile(declaration))
                    continue;

                VirtualFile declarationVirtualFile = OdinInsightUtils.getContainingVirtualFile(declaration);

                boolean samePackage = declarationVirtualFile.getParent().equals(thisParentPath);

                OdinImport odinImport = OdinImportUtils.computeRelativeImport(project,
                        thisVirtualFile,
                        declarationVirtualFile
                );

                if (odinImport != null) {
                    Path path = OdinImportUtils.getFirstAbsoluteImportPath(project, sourceFilePath, odinImport);
                    odinImport = importPathMap.getOrDefault(path, odinImport);
                } else if (!samePackage) {
                    continue;
                }
                processor.process(name, declaration, odinImport, completionResultSet);
            }

        }
    }

    interface ElementProcessor {
        boolean process(@NotNull String name,
                        @NotNull OdinDeclaration element,
                        @NotNull OdinImport importData,
                        @NotNull CompletionResultSet result);

    }

    static class CompletionsProcessor implements ElementProcessor {

        private final OdinFile sourceFile;
        private final OdinSymbolFilter symbolFilter;
        private final String sourcePackagePath;
        private final CompletionParameters parameters;

        public CompletionsProcessor(OdinFile sourceFile, OdinSymbolFilter symbolFilter, String sourcePackagePath, CompletionParameters parameters) {
            this.sourceFile = sourceFile;
            this.symbolFilter = symbolFilter;
            this.sourcePackagePath = sourcePackagePath;
            this.parameters = parameters;
        }

        @Override
        public boolean process(@NotNull String name,
                               @NotNull OdinDeclaration element,
                               @Nullable OdinImport importData,
                               @NotNull CompletionResultSet result) {
            String identifierName = substringAfter(name, '.');
            OdinSymbol symbol = OdinInsightUtils.createSymbol(element, identifierName);
            if (symbol == null)
                return false;

            if (!this.symbolFilter.shouldInclude(symbol))
                return false;

            if (!OdinInsightUtils.isVisible(sourceFile, symbol))
                return false;

            addLookUpElement(sourceFile, importData, sourcePackagePath, result, symbol, 0, parameters);
            return true;
        }

    }

    private static String substringBefore(@NotNull String s, char c) {
        int i = s.indexOf(c);
        if (i == -1) return s;
        return s.substring(0, i);
    }

    private static String substringAfter(@NotNull String s, char c) {
        int i = s.indexOf(c);
        if (i == -1) return "";
        return s.substring(i + 1);
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
