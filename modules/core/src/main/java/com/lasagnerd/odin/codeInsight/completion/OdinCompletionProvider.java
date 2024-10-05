package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.*;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinArrayType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.codeInsight.completion.CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED;
import static com.lasagnerd.odin.codeInsight.completion.OdinCompletionContributor.addLookUpElements;

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
        // Qualified types like 'package.<caret>'
        if (parent instanceof OdinSimpleRefType && parent.getParent() instanceof OdinQualifiedType qualifiedType) {
            addSelectorTypeCompletions(parameters, result, qualifiedType);
        }
        // Expressions like 'a.b.c.<caret>'
        else if (parent instanceof OdinRefExpression refExpression && refExpression.getExpression() != null) {
            addSelectorExpressionCompletions(parameters, result, refExpression);
        }
        // Identifiers like '<caret>'
        else {
            addIdentifierCompletions(parameters, result);
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
        if(symbol.getPsiType() != null) {
            if(symbol.getSymbolType() == OdinSymbolType.ENUM_FIELD) {
                OdinDeclaration declaration = PsiTreeUtil.getParentOfType(symbol.getPsiType(), OdinDeclaration.class);
                if (declaration != null) {
                    OdinDeclaredIdentifier declaredIdentifier = declaration.getDeclaredIdentifiers().getFirst();
                    typeText = declaredIdentifier.getName();
                }
            }
            if(symbol.getSymbolType() == OdinSymbolType.FIELD) {
                typeText = symbol.getPsiType().getText();
            }
        }
        return typeText;
    }

    private void addIdentifierCompletions(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        result.restartCompletionOnAnyPrefixChange();
        PsiElement position = parameters.getPosition();

        // 1. Add symbols from all visible stuff in my project
        OdinFile thisOdinFile = (OdinFile) parameters.getOriginalFile();
        VirtualFile thisFile = thisOdinFile.getVirtualFile();
        String thisPackagePath = thisFile.getParent().getPath();
        Project project = thisOdinFile.getProject();

        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
        // TODO Does this work if there is no source directory?
        VirtualFile sourceRoot = projectFileIndex.getSourceRootForFile(thisFile);
        String typed = position.getText().replaceAll(DUMMY_IDENTIFIER_TRIMMED + "$", "");
        if (sourceRoot != null && !typed.isBlank()) {
            // Recursively walk through all dirs starting from source root
            Stack<VirtualFile> work = new Stack<>();
            Map<String, List<OdinFile>> packages = new HashMap<>();
            work.add(sourceRoot);

            do {
                VirtualFile currentFile = work.pop();
                if (currentFile.isDirectory()) {
                    Collections.addAll(work, currentFile.getChildren());
                } else {
                    if (currentFile.getFileType() == OdinFileType.INSTANCE) {
                        String packagePath = currentFile.getParent().getPath();
                        OdinFile odinFile = (OdinFile) PsiManager.getInstance(project).findFile(currentFile);

                        if (!packagePath.isBlank()
                                && !packagePath.equals(sourceRoot.getPath())
                                && !packagePath.equals(thisFile.getParent().getPath())
                        ) {
                            packages.computeIfAbsent(packagePath, k -> new ArrayList<>()).add(odinFile);
                        }
                    }
                }
            } while (!work.isEmpty());

            // packages now contains all packages (recursively) under source root
            // 1. add all the symbols to the lookup elements whilst taking into consideration
            //  their origin package. Upon accepting a suggestion insert the import statement
            // if not already present

            for (Map.Entry<String, List<OdinFile>> entry : packages.entrySet()) {
                String packagePath = entry.getKey();
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

                    addLookUpElements(thisOdinFile,
                            thisPackagePath,
                            packagePath,
                            result,
                            visibleSymbols);
                }
            }
        }

        // 3. Add symbols from local scope
        OdinSymbolTable flatSymbolTable = OdinSymbolTableResolver.computeSymbolTable(position)
                .flatten();
        addLookUpElements(result, flatSymbolTable.getSymbols().stream()
                .filter(symbolFilter::shouldInclude)
                .toList());
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
