package com.lasagnerd.odin.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.codeInsight.imports.OdinImportInfo;
import com.lasagnerd.odin.codeInsight.symbols.*;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdinCompletionContributor extends CompletionContributor {


    public static final PsiElementPattern.@NotNull Capture<PsiElement> REFERENCE = psiElement().withElementType(OdinTypes.IDENTIFIER_TOKEN).afterLeaf(".");

    public static final @NotNull ElementPattern<PsiElement> AT_IDENTIFIER = psiElement()
            .withElementType(OdinTypes.IDENTIFIER_TOKEN)
            .andNot(REFERENCE);

    public OdinCompletionContributor() {

        // REFERENCE completion
        extend(CompletionType.BASIC,
                REFERENCE,
                new ReferenceCompletionProvider()
        );

        // Basic Completion
        extend(CompletionType.BASIC, AT_IDENTIFIER,
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getPosition();

                        // TODO This is probably where we will need stub indices / file based indices

                        // 1. Add symbols from all visible stuff in my project
                        OdinFile thisOdinFile = (OdinFile) parameters.getOriginalFile();
                        VirtualFile thisFile = thisOdinFile.getVirtualFile();
                        String thisPackagePath = thisFile.getParent().getPath();
                        Project project = thisOdinFile.getProject();

                        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
                        VirtualFile sourceRoot = projectFileIndex.getSourceRootForFile(thisFile);

                        if (sourceRoot != null) {
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

                                        if (!packagePath.isBlank() && !packagePath.equals(sourceRoot.getPath())) {
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
                                            .collect(Collectors.toList());

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
                        addLookUpElements(result, flatSymbolTable.getSymbols());
                    }
                }
        );

    }

    private static void addLookUpElements(@NotNull CompletionResultSet result, Collection<OdinSymbol> symbols) {
        addLookUpElements(null, "", "", result, symbols);
    }

    private static void addLookUpElements(OdinFile sourceFile,
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

                    OdinProcedureDeclarationStatement declaration = PsiTreeUtil.getParentOfType(declaredIdentifier, true, OdinProcedureDeclarationStatement.class);
                    if (declaration != null) {
                        element = procedureLookupElement(element, declaration)
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
                    OdinProcedureOverloadDeclarationStatement procedureOverloadStatement = PsiTreeUtil.getParentOfType(declaredIdentifier, true, OdinProcedureOverloadDeclarationStatement.class);
                    if (procedureOverloadStatement == null)
                        break;

                    for (OdinIdentifier odinIdentifier : procedureOverloadStatement.getIdentifierList()) {
                        var resolvedReference = odinIdentifier.getReference();

                        if (resolvedReference != null) {
                            PsiElement resolved = resolvedReference.resolve();
                            if (resolved instanceof OdinDeclaredIdentifier) {
                                OdinProcedureDeclarationStatement declaringProcedure = resolved.getParent() instanceof OdinProcedureDeclarationStatement ? (OdinProcedureDeclarationStatement) resolved.getParent() : null;
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
                                    result.addElement(PrioritizedLookupElement.withPriority(element, 0));
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

    public static Icon getIcon(OdinSymbolType typeType) {
        if (typeType == null)
            return AllIcons.FileTypes.Unknown;
        return switch (typeType) {
            case STRUCT -> OdinIcons.Types.Struct;
            case SWIZZLE_FIELD, FIELD -> AllIcons.Nodes.Property;
            case BIT_FIELD, ENUM_FIELD, LABEL, FOREIGN_IMPORT, BIT_SET, BUILTIN_TYPE -> null;
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
    private static LookupElementBuilder procedureLookupElement(LookupElementBuilder element, OdinProcedureDeclarationStatement declaringProcedure) {
        var params = declaringProcedure.getProcedureDefinition().getProcedureType().getParamEntryList();
        StringBuilder tailText = new StringBuilder("(");
        String paramList = params.stream().map(PsiElement::getText).collect(Collectors.joining(", "));
        tailText.append(paramList);
        tailText.append(")");
        element = element.withTailText(tailText.toString());

        OdinReturnParameters returnType = declaringProcedure.getProcedureDefinition().getProcedureType().getReturnParameters();
        if (returnType != null) {
            element = element.withTypeText(returnType.getText());
        }
        return element;
    }

    private static class ReferenceCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {

            // Walk up tree until no more ref expressions are found
            PsiElement position = parameters.getPosition();
            PsiElement parent = PsiTreeUtil.findFirstParent(position, e -> e instanceof OdinRefExpression);

            // This constitutes our scope
            if (parent instanceof OdinRefExpression reference) {
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
                                .collect(Collectors.toList());

                        addLookUpElements(result, visibleSymbols);
                    }
                }
            }

            OdinType parentType = PsiTreeUtil.getParentOfType(position, true, OdinType.class);
            if (parentType instanceof OdinSimpleRefType) {
                OdinSymbolTable symbolTable = OdinSymbolTableResolver.computeSymbolTable(parentType, parameters
                        .getOriginalFile()
                        .getContainingDirectory()
                        .getVirtualFile()
                        .getPath());
                OdinSymbolTable completionScope = OdinReferenceResolver.resolve(symbolTable, parentType);
                if (completionScope != null) {
                    addLookUpElements(result, completionScope.flatten().getSymbols());
                }
            }
        }
    }
}
