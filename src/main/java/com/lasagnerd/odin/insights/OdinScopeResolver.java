package com.lasagnerd.odin.insights;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

import static com.lasagnerd.odin.insights.OdinInsightUtils.*;

public class OdinScopeResolver {
    private final Predicate<PsiElement> matcher;
    private final Stack<ScopeNode> scopeNodes = new Stack<>();
    private final PsiElement element;

    public OdinScopeResolver(PsiElement element) {
        this.element = element;
        this.matcher = e -> true;
    }

    public OdinScopeResolver(PsiElement element, Predicate<PsiElement> matcher) {
        this.matcher = matcher;
        this.element = element;
    }

    public static Scope resolveScope(PsiElement element) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element);
        return odinScopeResolver.findScope();
    }

    public static Scope resolveScope(PsiElement element, Predicate<PsiElement> matcher) {
        OdinScopeResolver odinScopeResolver = new OdinScopeResolver(element, matcher);
        return odinScopeResolver.findScope();
    }

    private Scope findScope_legacy() {
        String packagePath = getPackagePath(element);

        findDeclaringBlocks(element);

        Scope fileScopeScope = new Scope();
        fileScopeScope.setPackagePath(packagePath);
        OdinFileScope fileScope = (OdinFileScope) PsiTreeUtil.findFirstParent(element, psi -> psi instanceof OdinFileScope);
        if (fileScope != null) {
            Scope fileScopeDeclarations = getFileScopeDeclarations(fileScope);
            fileScopeScope.addAll(fileScopeDeclarations.getFiltered(matcher), false);
            ScopeNode scopeNode = new ScopeNode();
            scopeNode.setLocalScope(fileScopeScope);
            scopeNode.setEntrance(fileScope);
            scopeNode.setRoot(true);
            scopeNodes.add(scopeNode);
        }

        // Here we can resolve all using nodes
        Scope resultScope = new Scope();
        for (int i = scopeNodes.size() - 1; i >= 0; i--) {
            ScopeNode scopeNode = scopeNodes.get(i);
            resultScope.addAll(scopeNode.getLocalScope().getNamedElements());

            for (OdinStatement statement : scopeNode.getStatements()) {
                if (statement instanceof OdinUsingStatement usingStatement) {
                    TypeInferenceResult typeInferenceResult = ExpressionTypeInference.inferType(resultScope, usingStatement.getExpression());
                    if (typeInferenceResult.type != null) {
                        Scope usingScope = getScopeProvidedByTypeExpression(typeInferenceResult.getType());
                        resultScope.addAll(usingScope.getNamedElements(), true);
                    }

                    if (typeInferenceResult.isImport) {
                        Scope declarationsOfImportedPackage = getDeclarationsOfImportedPackage(typeInferenceResult.getImportDeclarationStatement());
                        resultScope.addAll(declarationsOfImportedPackage.getNamedElements());
                    }
                }
            }
        }
        resultScope.setPackagePath(packagePath);
        return resultScope;
    }

    private Scope findScope() {
        String packagePath = getPackagePath(element);
        Scope scope = new Scope();
        scope.setPackagePath(packagePath);

        findDeclaringBlocks2(element);

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
            PsiElement containingBlock = scopeNode.getContainingBlock();
            Scope declarationsOfContainingBlock = getDeclarationsOfContainingBlock(containingBlock);
            scope.addAll(declarationsOfContainingBlock.getNamedElements());

            for (OdinStatement statement : scopeNode.getStatements()) {
                if (statement instanceof OdinDeclaration declaration) {
                    List<? extends PsiNamedElement> declaredIdentifiers = declaration.getDeclaredIdentifiers()
                            .stream().filter(matcher).toList();
                    scope.addAll(declaredIdentifiers, false);
                }

                if (statement instanceof OdinUsingStatement usingStatement) {
                    TypeInferenceResult typeInferenceResult = ExpressionTypeInference.inferType(scope, usingStatement.getExpression());
                    if (typeInferenceResult.type != null) {
                        Scope usingScope = getScopeProvidedByTypeExpression(typeInferenceResult.getType());
                        scope.addAll(usingScope.getNamedElements(), true);
                    }

                    if (typeInferenceResult.isImport) {
                        Scope declarationsOfImportedPackage = getDeclarationsOfImportedPackage(typeInferenceResult.getImportDeclarationStatement());
                        scope.addAll(declarationsOfImportedPackage.getNamedElements());
                    }
                }
            }
        }
        scope.setPackagePath(packagePath);
        return scope;
    }

    private void findDeclaringBlocks(PsiElement entrance) {
        Scope scope = new Scope();
        ScopeNode scopeNode = new ScopeNode();
        scopeNode.setLocalScope(scope);

        OdinBlock containingBlock = (OdinBlock) PsiTreeUtil.findFirstParent(entrance, true, parent -> parent instanceof OdinBlock);
        if (containingBlock != null) {
            OdinStatement containingStatement = findFirstParentOfType(entrance, false, OdinStatement.class);
            OdinStatement lastValidStatement;
            if (PsiTreeUtil.isAncestor(containingBlock, containingStatement, true)) {
                // This means the containing statement is inside the containing block
                lastValidStatement = containingStatement;
            } else {
                lastValidStatement = null;
            }

            boolean blockIsInsideProcedure = false;

            // If we are inside a procedure body we also add (return) parameters
            // We don't further look for scope, because a procedure is not a closure in Odin
            if (containingBlock.getParent() instanceof OdinProcedureBody procedureBody) {
                blockIsInsideProcedure = true;
                OdinProcedureExpression procedureExpression = findFirstParentOfType(procedureBody, true, OdinProcedureExpression.class);
                OdinProcedureType procedureType = null;
                if (procedureExpression != null) {
                    procedureType = procedureExpression.getProcedureExpressionType().getProcedureType();
                }

                if (procedureType == null) {
                    OdinProcedureDeclarationStatement procedureDeclarationStatement = findFirstParentOfType(procedureBody, true, OdinProcedureDeclarationStatement.class);
                    procedureType = procedureDeclarationStatement.getProcedureType();
                }

                OdinParamEntries paramEntries = procedureType.getParamEntries();
                if (paramEntries != null) {
                    for (OdinParamEntry odinParamEntry : paramEntries.getParamEntryList()) {
                        List<OdinParameter> parameters = odinParamEntry
                                .getParameterDeclaration()
                                .getParameterList();

                        parameters.stream()
                                .map(OdinParameter::getDeclaredIdentifier)
                                .filter(matcher)
                                .forEach(identifier -> scope.add(identifier, false));

                    }
                }

                OdinReturnParameters returnParameters = procedureType.getReturnParameters();
                if (returnParameters != null) {
                    OdinParamEntries returnParamEntries = returnParameters.getParamEntries();
                    if (returnParamEntries != null) {
                        for (OdinParamEntry odinParamEntry : returnParamEntries.getParamEntryList()) {
                            scope.addAll(odinParamEntry
                                    .getParameterDeclaration()
                                    .getDeclaredIdentifiers().stream()
                                    .filter(matcher)
                                    .toList(), false);
                        }
                    }
                }
            } else {
                // Add declarations of block (e.g. parameters, return parameters, if-block declarations, etc)
                if (containingBlock.getParent() instanceof OdinIfStatement ifStatement) {
                    OdinStatement statement = ifStatement.getCondition().getStatement();
                    scope.addAll(getNamedElements(matcher, statement));
                }

                if (containingBlock.getParent() instanceof OdinWhenStatement whenStatement) {
                    OdinStatement statement = whenStatement.getCondition().getStatement();
                    scope.addAll(getNamedElements(matcher, statement));
                }

                if (containingBlock.getParent() instanceof OdinForStatement ifStatement) {
                    OdinStatement statement = ifStatement.getForHead().getStatement();
                    scope.addAll(getNamedElements(matcher, statement));
                }
            }

            boolean afterStatement = false;
            for (OdinStatement statement : containingBlock.getStatements()) {
                if (!afterStatement) {
                    if (statement instanceof OdinDeclaration declaration) {
                        scope.addAll(declaration.getDeclaredIdentifiers(), false);
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
                    if (statement instanceof OdinDeclaration declaration) {
                        List<? extends PsiNamedElement> declaredIdentifiers = declaration.getDeclaredIdentifiers()
                                .stream().filter(matcher).toList();
                        scope.addAll(declaredIdentifiers, false);
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

    private void findDeclaringBlocks2(PsiElement entrance) {
        ScopeNode scopeNode = new ScopeNode();
        OdinBlock containingBlock = (OdinBlock) PsiTreeUtil.findFirstParent(entrance, true, parent -> parent instanceof OdinBlock);
        if (containingBlock != null) {
            scopeNode.setContainingBlock(containingBlock);
            OdinStatement containingStatement = findFirstParentOfType(entrance, false, OdinStatement.class);
            OdinStatement lastValidStatement;
            if (PsiTreeUtil.isAncestor(containingBlock, containingStatement, true)) {
                // This means the containing statement is inside the containing block
                lastValidStatement = containingStatement;
            } else {
                lastValidStatement = null;
            }

            boolean blockIsInsideProcedure = containingBlock.getParent() instanceof OdinProcedureBody;
            // If we are inside a procedure body we also add (return) parameters
            // We don't further look for scope, because a procedure is not a closure in Odin

            boolean afterStatement = false;
            for (OdinStatement statement : containingBlock.getStatements()) {
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
                findDeclaringBlocks2(containingBlock);
            }
        }
    }

    public Scope getDeclarationsOfContainingBlock(PsiElement containingBlock) {
        Scope scope = new Scope();
        if (containingBlock.getParent() instanceof OdinProcedureType procedureBody) {

            OdinProcedureExpression procedureExpression = findFirstParentOfType(procedureBody, true, OdinProcedureExpression.class);
            OdinProcedureType procedureType = null;
            if (procedureExpression != null) {
                procedureType = procedureExpression.getProcedureExpressionType().getProcedureType();
            }

            if (procedureType == null) {
                OdinProcedureDeclarationStatement procedureDeclarationStatement = findFirstParentOfType(procedureBody, true, OdinProcedureDeclarationStatement.class);
                procedureType = procedureDeclarationStatement.getProcedureType();
            }

            OdinParamEntries paramEntries = procedureType.getParamEntries();
            if (paramEntries != null) {
                for (OdinParamEntry odinParamEntry : paramEntries.getParamEntryList()) {
                    List<OdinParameter> parameters = odinParamEntry
                            .getParameterDeclaration()
                            .getParameterList();

                    parameters.stream()
                            .map(OdinParameter::getDeclaredIdentifier)
                            .filter(matcher)
                            .forEach(identifier -> scope.add(identifier, false));

                }
            }

            OdinReturnParameters returnParameters = procedureType.getReturnParameters();
            if (returnParameters != null) {
                OdinParamEntries returnParamEntries = returnParameters.getParamEntries();
                if (returnParamEntries != null) {
                    for (OdinParamEntry odinParamEntry : returnParamEntries.getParamEntryList()) {
                        scope.addAll(odinParamEntry
                                .getParameterDeclaration()
                                .getDeclaredIdentifiers().stream()
                                .filter(matcher)
                                .toList(), false);
                    }
                }
            }
        }

        if (containingBlock.getParent() instanceof OdinIfStatement ifStatement) {
            OdinStatement statement = ifStatement.getCondition().getStatement();
            scope.addAll(getNamedElements(matcher, statement));
        }

        if (containingBlock.getParent() instanceof OdinWhenStatement whenStatement) {
            OdinStatement statement = whenStatement.getCondition().getStatement();
            scope.addAll(getNamedElements(matcher, statement));
        }

        if (containingBlock.getParent() instanceof OdinForStatement ifStatement) {
            OdinStatement statement = ifStatement.getForHead().getStatement();
            scope.addAll(getNamedElements(matcher, statement));
        }

        return scope;
    }

    @Data
    static class ScopeNode {
        PsiElement entrance;
        Scope localScope;
        OdinStatement endOfScopeStatement;
        boolean isRoot;
        PsiElement containingBlock;
        List<OdinDeclaration> blockDeclarations; // Stuff like parameters and the declarations in an if- or for-statement
        List<OdinStatement> statements = new ArrayList<>();

        public void addStatement(OdinStatement statement) {
            statements.add(statement);
        }
    }
}

