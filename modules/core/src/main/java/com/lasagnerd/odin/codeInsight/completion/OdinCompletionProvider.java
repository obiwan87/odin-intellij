package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import com.lasagnerd.odin.codeInsight.symbols.*;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinEnumType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.codeInsight.completion.CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED;
import static com.lasagnerd.odin.codeInsight.completion.OdinCompletionContributor.addLookUpElements;
import static com.lasagnerd.odin.codeInsight.completion.OdinCompletionContributor.getIcon;

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
        OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(parentType, parameters
                .getOriginalFile()
                .getContainingDirectory()
                .getVirtualFile()
                .getPath());
        OdinSymbolTable completionScope = OdinReferenceResolver.resolve(symbolTable, parentType);
        if (completionScope != null) {
            addLookUpElements(result, completionScope.flatten()
                    .getSymbols()
                    .stream()
                    .filter(symbolFilter::shouldInclude)
                    .toList());
        }
    }

    private void addSelectorExpressionCompletions(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, OdinRefExpression reference) {
        // This constitutes our scope
        {
            OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(reference, parameters
                    .getOriginalFile()
                    .getContainingDirectory()
                    .getVirtualFile()
                    .getPath());

            if (reference.getExpression() != null) {
                // TODO at some point we should return the type of each symbol
                OdinSymbolTable completionScope = OdinReferenceResolver.resolve(symbolTable, reference.getExpression());
                if (completionScope != null) {
                    Collection<OdinSymbol> visibleSymbols = completionScope.flatten()
                            .getSymbols()
                            .stream()
                            .filter(s -> s.getSymbolType() != OdinSymbolType.PACKAGE_REFERENCE)
                            .filter(symbolFilter::shouldInclude)
                            .collect(Collectors.toList());

                    addLookUpElements(result, visibleSymbols);
                }
            }
        }
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        if (!(parameters.getPosition().getParent() instanceof OdinIdentifier identifier))
            return;


        // Stuff inside arrays, struct init blocks, etc.
        OdinRefExpression topMostRefExpression = OdinInsightUtils.findTopMostRefExpression(identifier);
        if (topMostRefExpression != null && (topMostRefExpression.getParent() instanceof OdinLhs || topMostRefExpression.getParent() instanceof OdinRhs)) {
            OdinCompoundLiteral compoundLiteral = PsiTreeUtil.getParentOfType(topMostRefExpression, OdinCompoundLiteral.class);
            if (compoundLiteral != null) {
                addCompoundLiteralCompletions(result, topMostRefExpression, compoundLiteral);
            }
        }

        PsiElement parent = identifier.getParent();

        switch (parent) {
            case OdinImplicitSelectorExpression implicitSelectorExpression -> {
                TsOdinType tsOdinType = OdinInferenceEngine
                        .inferExpectedType(
                                OdinSymbolTableResolver.computeSymbolTable(parameters.getPosition()),
                                implicitSelectorExpression
                        );

                if (tsOdinType instanceof TsOdinEnumType tsOdinEnumType) {
                    OdinSymbolTable typeElements = OdinInsightUtils.getTypeElements(parameters.getEditor().getProject(), tsOdinEnumType);
                    // Sort by definition order
                    List<OdinSymbol> list = typeElements.getSymbols().stream()
                            .sorted(
                                    Comparator.comparing(s -> s.getDeclaration().getTextOffset())
                            )
                            .toList();
                    for (int i = 0; i < list.size(); i++) {
                        OdinSymbol symbol = list.get(i);
                        LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                                .create(symbol.getName())
                                .withTypeText(tsOdinEnumType.getName())
                                .withPresentableText("." + symbol.getName())
                                .withIcon(getIcon(OdinSymbolType.ENUM_FIELD));

                        // Higher for earlier elements
                        result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, list.size() - i));
                    }
                }
            }
            // Qualified types like 'package.<caret>'
            case OdinSimpleRefType ignored when parent.getParent() instanceof OdinQualifiedType qualifiedType ->
                    addSelectorTypeCompletions(parameters, result, qualifiedType);

            // Expressions like 'a.b.c.<caret>'
            case OdinRefExpression refExpression when refExpression.getExpression() != null ->
                    addSelectorExpressionCompletions(parameters, result, refExpression);

            // Identifiers like '<caret>'
            case null, default -> addIdentifierCompletions(parameters, result);
        }
    }

    private static void addCompoundLiteralCompletions(@NotNull CompletionResultSet result, OdinRefExpression topMostRefExpression, OdinCompoundLiteral compoundLiteral) {
        OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(topMostRefExpression);
        TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(symbolTable, compoundLiteral);

        List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, symbolTable);

        for (OdinSymbol symbol : elementSymbols) {
            Icon icon = OdinCompletionContributor.getIcon(symbol.getSymbolType());
            String text = symbol.getSymbolType() == OdinSymbolType.ENUM_FIELD ? "." + symbol.getName() : symbol.getName();

            String typeText = getTypeText(symbol);

            LookupElementBuilder element = LookupElementBuilder
                    .create(symbol.getDeclaredIdentifier(), text)
                    .withLookupString(symbol.getName())
                    .withIcon(icon)
                    .withTypeText(typeText)
                    .withTailText(" → " + tsOdinType.getLabel())
                    .withInsertHandler(
                            (insertionContext, item) -> {
                                Document document = insertionContext.getDocument();
                                document.insertString(insertionContext.getTailOffset(), " = ");
                                insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset());
                                PsiDocumentManager.getInstance(insertionContext.getProject()).commitDocument(document);
                            }
                    );

            LookupElement prioritized = PrioritizedLookupElement.withPriority(element, 10000);
            result.addElement(prioritized);
        }
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
            if (symbol.getSymbolType() == OdinSymbolType.FIELD) {
                typeText = symbol.getPsiType().getText();
            }
        }
        return typeText;
    }

    private void addIdentifierCompletions(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        result.endBatch();
        PsiElement position = parameters.getPosition();
        String typed = position.getText().replaceAll(DUMMY_IDENTIFIER_TRIMMED + "$", "");

        // Add symbols from all visible stuff in my project
        OdinFile thisOdinFile = (OdinFile) parameters.getOriginalFile();
        VirtualFile thisFile = thisOdinFile.getVirtualFile();
        String thisPackagePath = thisFile.getParent().getPath();
        Project project = thisOdinFile.getProject();

        if (typed.isBlank()) {
            result.restartCompletionOnAnyPrefixChange();
        }

        // Add symbols from local scope
        OdinSymbolTable flatSymbolTable = OdinSymbolTableResolver.computeSymbolTable(position)
                .flatten();
        addLookUpElements(result, flatSymbolTable.getSymbols().stream()
                .filter(symbolFilter::shouldInclude)
                .toList(), 2000);

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
                OdinSymbolTable fileScopeDeclarations = fileScope.getSymbolTable();

                List<OdinSymbol> visibleSymbols = fileScopeDeclarations
                        .getSymbols(OdinSymbol.OdinVisibility.PUBLIC)
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
}
