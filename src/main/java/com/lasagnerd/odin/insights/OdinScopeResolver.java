package com.lasagnerd.odin.insights;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.insights.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.insights.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.insights.typeInference.OdinTypeInferenceResult;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.insights.typeSystem.TsOdinType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

import static com.lasagnerd.odin.insights.OdinInsightUtils.*;

public class OdinScopeResolver {
    public static OdinScope resolveScope(PsiElement element) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element);
        return odinScopeResolver.findScope();
    }

    public static OdinScope resolveScope(PsiElement element, Predicate<PsiElement> matcher) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element, matcher);
        return odinScopeResolver.findScope();
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

    private OdinScope findScope() {
        String packagePath = getPackagePath(element);
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

        if(packagePath != null) {
            List<OdinFile> otherFilesInPackage = getOtherFilesInPackage(element.getProject(), packagePath, getFileName(element));
            for (OdinFile odinFile : otherFilesInPackage) {
                if (odinFile == null || odinFile.getFileScope() == null) {
                    continue;
                }
                Collection<OdinSymbol> fileScopeDeclarations = getFileScopeDeclarations(odinFile.getFileScope(), PACKAGE_VISIBLE_ELEMENTS_MATCHER);
                scope.addAll(fileScopeDeclarations);
            }
        }

        // Here we can resolve all using nodes
        for (int i = scopeNodes.size() - 1; i >= 0; i--) {
            ScopeNode scopeNode = scopeNodes.get(i);
            OdinScopeBlock containingBlock = scopeNode.getScopeBlock();
            OdinScope declarationsOfContainingBlock = getScopeOfContainingBlock(scope, containingBlock);
            scope.addNamedElements(declarationsOfContainingBlock.getNamedElements());

            for (OdinStatement statement : scopeNode.getStatements()) {
                if (statement instanceof OdinDeclaration declaration) {
                    List<? extends PsiNamedElement> declaredIdentifiers = declaration
                            .getDeclaredIdentifiers()
                            .stream().filter(matcher).toList();
                    scope.addNamedElements(declaredIdentifiers, false);
                }

                if (statement instanceof OdinUsingStatement usingStatement) {
                    OdinTypeInferenceResult typeInferenceResult = OdinInferenceEngine.inferType(scope, usingStatement.getExpression());
                    if (typeInferenceResult.getType() != null) {
                        OdinScope usingScope = getScopeProvidedByType(typeInferenceResult.getType());
                        scope.addNamedElements(usingScope.getNamedElements(), true);
                    }

                    if (typeInferenceResult.isImport()) {
                        OdinScope declarationsOfImportedPackage = getDeclarationsOfImportedPackage(typeInferenceResult.getImportDeclarationStatement());
                        scope.putAll(declarationsOfImportedPackage);
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

    /**
     * Gets the files in the indicated package but excludes the file fileName
     *
     * @param project The current project
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
            if(symbol.isHasUsing()) {
                if(symbol.getType() != null) {
                    TsOdinType tsOdinType = OdinTypeResolver.resolveType(parentScope, symbol.getType());
                    scope.putAll(getScopeProvidedByType(tsOdinType));
                } else {
                    if(symbol.getValueExpression() != null) {
                        OdinTypeInferenceResult typeInferenceResult = OdinInferenceEngine.inferType(parentScope, symbol.getValueExpression());
                        TsOdinType type = typeInferenceResult.getType();
                        if(type != null) {
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

