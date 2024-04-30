package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.lang.typeSystem.TsOdinType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

import static com.lasagnerd.odin.insights.OdinInsightUtils.*;

public class OdinScopeResolver {
    public static Scope resolveScope(PsiElement element) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element);
        return odinScopeResolver.findScope();
    }

    public static Scope resolveScope(PsiElement element, Predicate<PsiElement> matcher) {
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

    private Scope findScope() {
        String packagePath = getPackagePath(element);
        Scope scope = new Scope();
        scope.setPackagePath(packagePath);

        findDeclaringBlocks(element);

        Scope fileScopeScope = new Scope();
        fileScopeScope.setPackagePath(packagePath);
        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(element, psi -> psi instanceof OdinFileScope);
        if (fileScope != null) {
            Scope fileScopeDeclarations = getFileScopeDeclarations(fileScope);
            scope.addAll(fileScopeDeclarations.getFiltered(matcher), false);
        }

        // Here we can resolve all using nodes

        for (int i = scopeNodes.size() - 1; i >= 0; i--) {
            ScopeNode scopeNode = scopeNodes.get(i);
            OdinScopeBlock containingBlock = scopeNode.getScopeBlock();
            Scope declarationsOfContainingBlock = getDeclarationsOfContainingBlock(scope, containingBlock);
            scope.addAll(declarationsOfContainingBlock.getNamedElements());

            for (OdinStatement statement : scopeNode.getStatements()) {
                if (statement instanceof OdinDeclaration declaration) {
                    List<? extends PsiNamedElement> declaredIdentifiers = declaration.getDeclaredIdentifiers()
                            .stream().filter(matcher).toList();
                    scope.addAll(declaredIdentifiers, false);
                }

                if (statement instanceof OdinUsingStatement usingStatement) {
                    TypeInferenceResult typeInferenceResult = OdinExpressionTypeResolver.inferType(scope, usingStatement.getExpression());
                    if (typeInferenceResult.type != null) {
                        Scope usingScope = getScopeProvidedByType(typeInferenceResult.getType());
                        scope.addAll(usingScope.getNamedElements(), true);
                    }

                    if (typeInferenceResult.isImport) {
                        Scope declarationsOfImportedPackage = getDeclarationsOfImportedPackage(typeInferenceResult.getImportDeclarationStatement());
                        scope.addAll(declarationsOfImportedPackage.getNamedElements());
                    }
                }

                if (statement instanceof OdinVariableInitializationStatement variableInitializationStatement) {
                    if (variableInitializationStatement.getUsing() != null) {
                        OdinTypeDefinitionExpression typeDefinitionExpression = variableInitializationStatement.getTypeDefinitionExpression();
                        if (typeDefinitionExpression != null) {
                            OdinTypeExpression mainTypeExpression = typeDefinitionExpression.getMainTypeExpression();
                            TsOdinType tsOdinType = OdinTypeExpressionResolver.resolveType(scope, mainTypeExpression);
                            if (tsOdinType != null) {
                                Scope scopeProvidedByType = getScopeProvidedByType(tsOdinType);
                                scope.addAll(scopeProvidedByType.getNamedElements());
                            }
                        } else {
                            List<OdinExpression> expressionList = variableInitializationStatement.getExpressionsList().getExpressionList();
                            if (!expressionList.isEmpty()) {
                                OdinExpression odinExpression = expressionList.get(0);
                                TypeInferenceResult typeInferenceResult = OdinExpressionTypeResolver.inferType(scope, odinExpression);
                                TsOdinType type = typeInferenceResult.getType();
                                if (type != null) {
                                    Scope scopeProvidedByType = getScopeProvidedByType(type);
                                    scope.addAll(scopeProvidedByType.getNamedElements());
                                }
                            }
                        }
                    }
                }

                if (statement instanceof OdinVariableDeclarationStatement variableDeclarationStatement) {
                    if (variableDeclarationStatement.getUsing() != null) {
                        OdinTypeExpression mainTypeExpression = variableDeclarationStatement.getTypeDefinitionExpression().getMainTypeExpression();
                        TsOdinType type = OdinTypeExpressionResolver.resolveType(scope, mainTypeExpression);
                        if (type != null) {
                            Scope scopeProvidedByType = getScopeProvidedByType(type);
                            scope.addAll(scopeProvidedByType.getNamedElements());
                        }
                    }
                }
            }
        }
        scope.setPackagePath(packagePath);
        return scope;
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

    public Scope getDeclarationsOfContainingBlock(Scope parentScope, OdinScopeBlock containingBlock) {
        Scope scope = new Scope();
        scope.setPackagePath(parentScope.getPackagePath());
        for (OdinDeclarationSpec declarationsSpec : containingBlock.getDeclarationsSpecs()) {
            scope.add(declarationsSpec.getDeclaredIdentifier());
            if(declarationsSpec.isHasUsing()) {
                if(declarationsSpec.getTypeDefinitionExpression() != null) {
                    TsOdinType tsOdinType = OdinTypeExpressionResolver.resolveType(parentScope, declarationsSpec.getTypeDefinitionExpression().getMainTypeExpression());
                    scope.addAll(getScopeProvidedByType(tsOdinType).getNamedElements());
                } else {
                    if(declarationsSpec.getValueExpression() != null) {
                        TypeInferenceResult typeInferenceResult = OdinExpressionTypeResolver.inferType(parentScope, declarationsSpec.getValueExpression());
                        TsOdinType type = typeInferenceResult.getType();
                        if(type != null) {
                            scope.addAll(getScopeProvidedByType(type).getNamedElements());
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

