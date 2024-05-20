package com.lasagnerd.odin.insights;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.insights.typeInference.OdinTypeInferenceResult;
import com.lasagnerd.odin.insights.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.insights.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static com.lasagnerd.odin.insights.OdinInsightUtils.*;

public class OdinScopeResolver {
    public static OdinScope resolveScope(PsiElement element) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element);
        return odinScopeResolver.findScope(getPackagePath(odinScopeResolver.element));
    }

    public static OdinScope resolveScope(PsiElement element, Predicate<PsiElement> matcher) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element, matcher);
        return odinScopeResolver.findScope(getPackagePath(odinScopeResolver.element));
    }

    public static OdinScope resolveScope(PsiElement element, String packagePath) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element);
        return odinScopeResolver.findScope(packagePath);
    }

    private final Predicate<PsiElement> matcher;
    private final Stack<ScopeNode> scopeNodes = new Stack<>();
    private final PsiElement element;

    private OdinScopeResolver(PsiElement element) {
        this.element = element;
        this.matcher = e -> true;
    }

    private OdinScopeResolver(PsiElement element, Predicate<PsiElement> matcher) {
        this.matcher = matcher;
        this.element = element;
    }

    private static OdinScope getFileScopeDeclarations(@NotNull OdinFileScope fileScope) {
        OdinSymbol.OdinVisibility globalFileVisibility = getGlobalFileVisibility(fileScope);
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

    public static Collection<OdinSymbol> getFileScopeDeclarations(OdinFileScope odinFileScope, Predicate<PsiElement> matcher) {
        return getFileScopeDeclarations(odinFileScope).getFiltered(matcher);
    }

    private OdinScope findScope(String packagePath) {
        OdinScope scope = new OdinScope();
        scope.setPackagePath(packagePath);

        findDeclaringBlocks(element);

        OdinScope fileScopeScope = new OdinScope();
        fileScopeScope.setPackagePath(packagePath);
        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(element, psi -> psi instanceof OdinFileScope);
        if (fileScope != null) {
            OdinScope fileScopeDeclarations = getFileScopeDeclarations(fileScope);
            scope.addAll(fileScopeDeclarations.getFiltered(matcher), false);
        }

        if (packagePath != null) {
            // Filter out symbols declared with private="file" or do not include anything if comment //+private is in front of package declaration
            List<OdinFile> otherFilesInPackage = getOtherFilesInPackage(element.getProject(), packagePath, getFileName(element));
            for (OdinFile odinFile : otherFilesInPackage) {
                if (odinFile == null || odinFile.getFileScope() == null) {
                    continue;
                }
                if (getGlobalFileVisibility(odinFile.getFileScope()) == OdinSymbol.OdinVisibility.FILE_PRIVATE) continue;
                Collection<OdinSymbol> fileScopeDeclarations = getFileScopeDeclarations(odinFile.getFileScope())
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
                      .filter(s -> matcher.test(s.getDeclaredIdentifier())).toList();

//                    List<? extends PsiNamedElement> declaredIdentifiers = declaration
//                            .getDeclaredIdentifiers()
//                            .stream().filter(matcher).toList();
                    scope.addAll(symbols, false);
                }

                if (statement instanceof OdinUsingStatement usingStatement) {
                    OdinTypeInferenceResult typeInferenceResult = OdinInferenceEngine.inferType(scope, usingStatement.getExpression());
                    if (typeInferenceResult.getType() != null) {
                        OdinScope usingScope = getScopeProvidedByType(typeInferenceResult.getType());
                        scope.putAll(usingScope);
                    }

                    if (typeInferenceResult.isImport()) {
                        OdinScope packageScope = getDeclarationsOfImportedPackage(typeInferenceResult.getImportDeclarationStatement());
                        scope.putAll(packageScope);
                    }
                }

                if (statement instanceof OdinVariableInitializationStatement variableInitializationStatement) {
                    if (variableInitializationStatement.getUsing() != null) {
                        OdinTypeDefinitionExpression typeDefinitionExpression = variableInitializationStatement.getTypeDefinitionExpression();
                        if (typeDefinitionExpression != null) {
                            OdinType mainTypeExpression = typeDefinitionExpression.getType();
                            TsOdinType tsOdinType = OdinTypeResolver.resolveType(scope, mainTypeExpression);
                            OdinScope scopeProvidedByType = getScopeProvidedByType(tsOdinType);
                            scope.putAll(scopeProvidedByType);
                        } else {
                            List<OdinExpression> expressionList = variableInitializationStatement.getExpressionsList().getExpressionList();
                            if (!expressionList.isEmpty()) {
                                OdinExpression odinExpression = expressionList.get(0);
                                OdinTypeInferenceResult typeInferenceResult = OdinInferenceEngine.inferType(scope, odinExpression);
                                TsOdinType type = typeInferenceResult.getType();
                                if (type != null) {
                                    OdinScope scopeProvidedByType = getScopeProvidedByType(type);
                                    scope.putAll(scopeProvidedByType);
                                }
                            }
                        }
                    }
                }

                if (statement instanceof OdinVariableDeclarationStatement variableDeclarationStatement) {
                    if (variableDeclarationStatement.getUsing() != null) {
                        OdinType mainTypeExpression = variableDeclarationStatement.getTypeDefinitionExpression().getType();
                        TsOdinType type = OdinTypeResolver.resolveType(scope, mainTypeExpression);
                        OdinScope scopeProvidedByType = getScopeProvidedByType(type);
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
                System.out.println("Line comment found");
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
        return getFilesInPackage(project, Path.of(packagePath), virtualFile -> !virtualFile.getName().equals(fileName));
    }

    private void findDeclaringBlocks(PsiElement entrance) {
        ScopeNode scopeNode = new ScopeNode();
        OdinScopeBlock containingBlock = OdinInsightUtils.findFirstParentOfType(entrance, true, OdinScopeBlock.class);

        if (containingBlock != null) {
            scopeNode.setScopeBlock(containingBlock);
            OdinStatement containingStatement = findFirstParentOfType(entrance, false, OdinStatement.class);
            OdinStatement lastValidStatement;
            if (PsiTreeUtil.isAncestor(containingBlock, containingStatement, true)) {
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
                if (symbol.getType() != null) {
                    TsOdinType tsOdinType = OdinTypeResolver.resolveType(parentScope, symbol.getType());
                    scope.putAll(getScopeProvidedByType(tsOdinType));
                } else {
                    if (symbol.getValueExpression() != null) {
                        OdinTypeInferenceResult typeInferenceResult = OdinInferenceEngine.inferType(parentScope, symbol.getValueExpression());
                        TsOdinType type = typeInferenceResult.getType();
                        if (type != null) {
                            scope.putAll(getScopeProvidedByType(type));
                        }
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

