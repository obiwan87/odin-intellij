package com.lasagnerd.odin.insights;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.insights.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.insights.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static com.lasagnerd.odin.insights.OdinInsightUtils.getScopeProvidedByType;

public class OdinScopeResolver {
    public static OdinScope resolveScope(PsiElement element) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element);
        return odinScopeResolver.findScope(OdinImportUtils.getPackagePath(odinScopeResolver.element));
    }

    public static OdinScope resolveScope(PsiElement element, Predicate<OdinSymbol> matcher) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element, matcher);
        return odinScopeResolver.findScope(OdinImportUtils.getPackagePath(odinScopeResolver.element));
    }

    private final Predicate<OdinSymbol> matcher;
    private final Stack<ScopeNode> scopeNodes = new Stack<>();
    private final PsiElement element;

    private OdinScopeResolver(PsiElement element) {
        this.element = element;
        this.matcher = e -> true;
    }

    private OdinScopeResolver(PsiElement element, Predicate<OdinSymbol> matcher) {
        this.matcher = matcher;
        this.element = element;
    }

    public static OdinScope getFileScopeDeclarations(@NotNull OdinFileScope fileScope, OdinSymbol.OdinVisibility globalFileVisibility) {
        // Find all blocks that are not in a procedure
        List<OdinSymbol> fileScopeSymbols = new ArrayList<>();

        Stack<PsiElement> statementStack = new Stack<>();

        // do bfs
        statementStack.addAll(fileScope.getStatementList());
        while (!statementStack.isEmpty()) {
            PsiElement element = statementStack.pop();
            if (element instanceof OdinDeclaration declaration) {
                List<OdinSymbol> symbols = OdinSymbolResolver.getSymbols(globalFileVisibility, declaration);
                fileScopeSymbols.addAll(symbols);
            } else {
                getStatements(element).forEach(statementStack::push);
            }
        }
        return OdinScope.from(fileScopeSymbols);
    }

    private static List<OdinStatement> getStatements(PsiElement psiElement) {
        if (psiElement instanceof OdinWhenStatement odinWhenStatement) {
            if (odinWhenStatement.getStatementBody().getBlock() != null) {
                OdinStatementList statementList = odinWhenStatement.getStatementBody().getBlock().getStatementList();
                if (statementList != null) {
                    return statementList.getStatementList();
                }
            }

            if (odinWhenStatement.getStatementBody().getDoStatement() != null) {
                return List.of(odinWhenStatement.getStatementBody().getDoStatement());
            }
        }

        if (psiElement instanceof OdinForeignStatement foreignStatement) {
            OdinForeignBlock foreignBlock = foreignStatement.getForeignBlock();
            OdinForeignStatementList foreignStatementList = foreignBlock.getForeignStatementList();
            if (foreignStatementList != null) {
                return foreignStatementList.getStatementList();
            }
        }

        return Collections.emptyList();
    }

    private static List<OdinSymbol> getBuiltInSymbols(Project project) {
        // TODO Cache this stuff
        List<OdinSymbol> builtinSymbols = new ArrayList<>();
        // 0. Import built-in symbols
        Optional<String> sdkPathOptional = OdinSdkConfigPersistentState.getSdkPath(project);

        if (sdkPathOptional.isEmpty())
            return Collections.emptyList();

        String sdkPath = sdkPathOptional.get();
        Path coreBuiltinPath = Path.of(sdkPath, "base", "runtime", "core_builtin.odin");
        Path coreBuiltinSoaPath = Path.of(sdkPath, "base", "runtime", "core_builtin_soa.odin");

        List<Path> builtinPaths = List.of(coreBuiltinPath, coreBuiltinSoaPath);
        for (Path builtinPath : builtinPaths) {
            OdinFile odinFile = createOdinFile(project, builtinPath);
            if (odinFile != null) {
                OdinScope fileScopeDeclarations = getFileScopeDeclarations(odinFile.getFileScope(), getGlobalFileVisibility(odinFile.getFileScope()));
                Collection<OdinSymbol> symbols = fileScopeDeclarations
                        .getSymbolTable().values()
                        .stream()
                        .filter(odinSymbol -> OdinAttributeUtils.containsBuiltin(odinSymbol.getAttributeStatements()))
                        .toList();
                builtinSymbols.addAll(symbols);
            }
        }

        List<String> resources = List.of("odin/builtin.odin", "odin/intrinsics.odin");
        for (String resource : resources) {
            OdinFile odinFile = createOdinFileFromResource(project, resource);
            if (odinFile != null) {
                OdinScope fileScopeDeclarations = getFileScopeDeclarations(odinFile.getFileScope(), getGlobalFileVisibility(odinFile.getFileScope()));
                Collection<OdinSymbol> symbols = fileScopeDeclarations
                        .getSymbolTable().values()
                        .stream()
                        .filter(odinSymbol -> OdinAttributeUtils.containsBuiltin(odinSymbol.getAttributeStatements()))
                        .toList();
                builtinSymbols.addAll(symbols);
            }
        }
        return builtinSymbols;
    }

    private static OdinFile createOdinFile(Project project, Path path) {
        VirtualFile virtualFile = VfsUtil.findFile(path, true);
        if (virtualFile != null) {
            return (OdinFile) PsiManager.getInstance(project).findFile(virtualFile);
        }
        return null;
    }

    private static OdinFile createOdinFileFromResource(Project project, String resourcePath) {
        InputStream resource = OdinScopeResolver.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resource == null)
            return null;
        try (resource) {
            String text = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            return (OdinFile) PsiFileFactory.getInstance(project).createFileFromText("resource.odin", OdinFileType.INSTANCE, text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OdinScope findScope(String packagePath) {
        Project project = element.getProject();

        OdinScope scope = new OdinScope();
        scope.setPackagePath(packagePath);

        // Build the scope tree
        findDeclaringBlocks(element);

        List<OdinSymbol> builtInSymbols = getBuiltInSymbols(project);

        // 0. Import built-in symbols
        scope.addAll(builtInSymbols);

        // 1. Import symbols from this file
        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(element, psi -> psi instanceof OdinFileScope);

        if (fileScope != null) {
            OdinScope fileScopeDeclarations = getFileScopeDeclarations(fileScope, getGlobalFileVisibility(fileScope));
            scope.addAll(fileScopeDeclarations.getFilteredSymbols(matcher), false);
        }

        // 2. Import symbols from other files in the same package
        if (packagePath != null) {
            // Filter out symbols declared with private="file" or do not include anything if comment //+private is in front of package declaration
            List<OdinFile> otherFilesInPackage = getOtherFilesInPackage(project, packagePath, OdinImportUtils.getFileName(element));
            for (OdinFile odinFile : otherFilesInPackage) {
                if (odinFile == null || odinFile.getFileScope() == null) {
                    continue;
                }
                OdinSymbol.OdinVisibility globalFileVisibility = getGlobalFileVisibility(odinFile.getFileScope());
                if (globalFileVisibility == OdinSymbol.OdinVisibility.FILE_PRIVATE) continue;
                Collection<OdinSymbol> fileScopeDeclarations = getFileScopeDeclarations(odinFile.getFileScope(), globalFileVisibility)
                        .getSymbolTable()
                        .values()
                        .stream()
                        .filter(o -> !o.getVisibility().equals(OdinSymbol.OdinVisibility.FILE_PRIVATE))
                        .toList();


                scope.addAll(fileScopeDeclarations);
            }
        }

        // Here we can resolve all using nodes
        for (int i = scopeNodes.size() - 1; i >= 0; i--) {
            ScopeNode scopeNode = scopeNodes.get(i);
            OdinScopeBlock containingBlock = scopeNode.getScopeBlock();
            OdinScope declarationsOfContainingBlock = getScopeOfContainingBlock(scope, containingBlock);
            scope.putAll(declarationsOfContainingBlock);

            for (OdinStatement statement : scopeNode.getStatements()) {
                if (statement instanceof OdinDeclaration declaration) {
                    List<OdinSymbol> symbols = OdinSymbolResolver.getSymbols(declaration)
                            .stream()
                            .filter(matcher)
                            .toList();

                    scope.addAll(symbols, false);
                }

                if (statement instanceof OdinUsingStatement usingStatement) {
                    var type = OdinInferenceEngine.inferType(scope, usingStatement.getExpression());
                    OdinScope usingScope = getScopeProvidedByType(type);
                    scope.putAll(usingScope);
                }

                if (statement instanceof OdinVariableInitializationStatement variableInitializationStatement) {
                    if (variableInitializationStatement.getUsing() != null) {
                        OdinType type = variableInitializationStatement.getType();
                        if (type != null) {
                            TsOdinType tsOdinType = OdinTypeResolver.resolveType(scope, type);
                            OdinScope scopeProvidedByType = getScopeProvidedByType(tsOdinType);
                            scope.putAll(scopeProvidedByType);
                        } else {
                            List<OdinExpression> expressionList = variableInitializationStatement.getExpressionsList().getExpressionList();
                            if (!expressionList.isEmpty()) {
                                OdinExpression odinExpression = expressionList.get(0);
                                TsOdinType tsOdinType = OdinInferenceEngine.inferType(scope, odinExpression);
                                OdinScope scopeProvidedByType = getScopeProvidedByType(tsOdinType);
                                scope.putAll(scopeProvidedByType);
                            }
                        }
                    }
                }

                if (statement instanceof OdinVariableDeclarationStatement variableDeclarationStatement) {
                    if (variableDeclarationStatement.getUsing() != null) {
                        OdinType psiType = variableDeclarationStatement.getType();
                        TsOdinType tsOdinType = OdinTypeResolver.resolveType(scope, psiType);
                        OdinScope scopeProvidedByType = getScopeProvidedByType(tsOdinType);
                        scope.putAll(scopeProvidedByType);
                    }
                }
            }
        }
        scope.setPackagePath(packagePath);
        return scope;
    }

    public static OdinSymbol.OdinVisibility getGlobalFileVisibility(OdinFileScope fileScope) {
        PsiElement lineComment = PsiTreeUtil.skipSiblingsBackward(fileScope, PsiWhiteSpace.class);
        if (lineComment != null) {
            IElementType elementType = PsiUtilCore.getElementType(lineComment.getNode());
            if (elementType == OdinTypes.LINE_COMMENT) {
                if (lineComment.getText().equals("//+private")) {
                    return OdinSymbol.OdinVisibility.PACKAGE_PRIVATE;
                }

                if (lineComment.getText().equals("//+private file")) {
                    return OdinSymbol.OdinVisibility.FILE_PRIVATE;
                }
            }
        }
        return null;
    }

    /**
     * Gets the files in the indicated package but excludes the file fileName
     *
     * @param project     The current project
     * @param packagePath The packagePath
     * @param fileName    The file to exclude
     * @return Other files in package
     */
    private static @NotNull List<OdinFile> getOtherFilesInPackage(@NotNull Project project, @NotNull String packagePath, String fileName) {
        return OdinImportUtils.getFilesInPackage(project, Path.of(packagePath), virtualFile -> !virtualFile.getName().equals(fileName));
    }

    private void findDeclaringBlocks(PsiElement entrance) {
        ScopeNode scopeNode = new ScopeNode();
        OdinScopeBlock containingBlock = PsiTreeUtil.getParentOfType(entrance, true, OdinScopeBlock.class);

        if (containingBlock != null) {
            scopeNode.setScopeBlock(containingBlock);
            OdinStatement containingStatement = PsiTreeUtil.getParentOfType(entrance, false, OdinStatement.class);
            OdinStatement lastValidStatement;
            if (containingStatement != null && PsiTreeUtil.isAncestor(containingBlock, containingStatement, true)) {
                // This means the containing statement is inside the containing block
                lastValidStatement = containingStatement;
            } else {
                lastValidStatement = null;
            }

            boolean blockIsInsideProcedure = containingBlock instanceof OdinProcedureExpression
                    || containingBlock instanceof OdinProcedureDeclarationStatement;
            // If we are inside a procedure body we also add (return) parameters
            // We don't further look for scope, because a procedure is not a closure in Odin

            boolean afterStatement = false;
            for (OdinStatement statement : containingBlock.getBlockStatements()) {
                if (!afterStatement) {
                    if (statement instanceof OdinDeclaration) {
                        scopeNode.addStatement(statement);
                    }

                    if (statement instanceof OdinUsingStatement usingStatement) {
                        scopeNode.addStatement(usingStatement);
                    }
                } else {
                    // Here we only add stuff that is not a variable initialization or declaration
                    // Background: Constants and type definitions are available in the entire block, no matter where they
                    // were declared.
                    if (statement instanceof OdinVariableDeclarationStatement)
                        continue;
                    if (statement instanceof OdinVariableInitializationStatement) {
                        continue;
                    }
                    if (statement instanceof OdinDeclaration) {
                        scopeNode.addStatement(statement);
                    }
                }
                if (statement == lastValidStatement) {
                    afterStatement = true;
                }
            }

            scopeNode.setEndOfScopeStatement(lastValidStatement);
            scopeNodes.add(scopeNode);
            if (!blockIsInsideProcedure) {
                findDeclaringBlocks(containingBlock);
            }
        }
    }

    public OdinScope getScopeOfContainingBlock(OdinScope parentScope, OdinScopeBlock containingBlock) {
        OdinScope scope = new OdinScope();
        scope.setPackagePath(parentScope.getPackagePath());
        for (OdinSymbol symbol : containingBlock.getSymbols()) {
            scope.add(symbol.getDeclaredIdentifier());
            if (symbol.isHasUsing()) {
                if (symbol.getPsiType() != null) {
                    TsOdinType tsOdinType = OdinTypeResolver.resolveType(parentScope, symbol.getPsiType());
                    scope.putAll(getScopeProvidedByType(tsOdinType));
                } else {
                    if (symbol.getValueExpression() != null) {
                        TsOdinType type = OdinInferenceEngine.inferType(parentScope, symbol.getValueExpression());
                        scope.putAll(getScopeProvidedByType(type));
                    }
                }
            }
        }
        return scope;
    }

    @Data
    static class ScopeNode {
        OdinStatement endOfScopeStatement;
        OdinScopeBlock scopeBlock;
        List<OdinStatement> statements = new ArrayList<>();

        public void addStatement(OdinStatement statement) {
            statements.add(statement);
        }
    }
}

