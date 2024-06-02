package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class OdinSymbolFinder {
    @FunctionalInterface
    public interface ScopeCondition {
        boolean match(OdinScope scope);
    }

    public static OdinScope doFindVisibleSymbols(PsiElement position) {
        return doFindVisibleSymbols(position, scope -> false);
    }

    public static OdinScope doFindVisibleSymbols(PsiElement position, ScopeCondition scopeCondition) {
        // 1. Find the starting point
        //  = a statement whose parent is a scope block
        // 2. Get the parent and define and get all declarations inside the scope block
        // 3. Add all constant declarations as they are not dependent on the position within the scope block
        // 4. Add all non-constant declarations, depending on whether the position is before or after
        //    the declared symbol

        OdinScopeArea containingScopeBlock = PsiTreeUtil.getParentOfType(position, OdinScopeArea.class);

        if (containingScopeBlock == null)
            return OdinScope.EMPTY;

        if (containingScopeBlock instanceof OdinFileScope odinFileScope) {
            return OdinScopeResolver.getFileScopeDeclarations(odinFileScope);
        }

        OdinScope scope = new OdinScope();
        OdinScope parentScope = doFindVisibleSymbols(containingScopeBlock, scopeCondition);
        scope.setParentScope(parentScope);

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        List<OdinDeclaration> declarations = getDeclarations(containingScopeBlock);

        for (OdinDeclaration declaration : declarations) {
            PositionCheckResult positionCheckResult = checkPosition(position, declaration, declaration);
            if (!positionCheckResult.validPosition)
                continue;

            if (declaration instanceof OdinConstantDeclaration constantDeclaration) {
                // TODO these could also be global symbols, in fact, we can generalize getFileScopeDeclarations to this
                List<OdinSymbol> localSymbols = OdinSymbolResolver.getLocalSymbols(constantDeclaration, scope);
                scope.addAll(localSymbols);
            }

            if(scopeCondition.match(scope))
                return scope;
        }


        for (var declaration : declarations) {
            if(declaration instanceof OdinConstantDeclaration)
                continue;
            List<OdinSymbol> localSymbols = OdinSymbolResolver.getLocalSymbols(declaration);
            for (OdinSymbol symbol : localSymbols) {
                PositionCheckResult positionCheckResult = checkPosition(position, symbol.getDeclaredIdentifier(), declaration);
                if (!positionCheckResult.validPosition)
                    continue;

                PsiElement commonParent = positionCheckResult.commonParent();
                PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();
                PsiElement containerOfSymbol = PsiTreeUtil.findPrevParent(commonParent, declaredIdentifier);
                PsiElement containerOfPosition = PsiTreeUtil.findPrevParent(commonParent, position);

                // Now check if symbol is strictly a previous sibling of position
                List<@NotNull PsiElement> childrenList = Arrays.stream(commonParent.getChildren()).toList();
                int indexOfSymbol = childrenList.indexOf(containerOfSymbol);
                int indexOfPosition = childrenList.indexOf(containerOfPosition);

                if (indexOfPosition > indexOfSymbol) {
                    scope.add(symbol);
                }

                if (scopeCondition.match(scope))
                    return scope;
            }
        }

        return scope;
    }

    private static @NotNull List<OdinDeclaration> getDeclarations(PsiElement containingScopeBlock) {
        List<OdinDeclaration> declarations = new ArrayList<>();
        if (containingScopeBlock instanceof OdinStatementList statementList) {
            for (OdinStatement odinStatement : statementList.getStatementList()) {
                if (odinStatement instanceof OdinDeclaration declaration) {
                    declarations.add(declaration);
                }
            }
        }

        if (containingScopeBlock instanceof OdinIfBlock odinIfBlock) {
            addControlFlowInit(odinIfBlock.getControlFlowInit(), declarations);
        }

        if (containingScopeBlock instanceof OdinProcedureDefinition procedureDefinition) {
            OdinProcedureType procedureType = procedureDefinition.getProcedureType();

            addParamEntries(procedureType.getParamEntries(), declarations);
            addPolymorphicDeclarations(procedureType.getParamEntries(), declarations);

            if (procedureType.getReturnParameters() != null) {
                OdinParamEntries returnParamEntries = procedureType.getReturnParameters().getParamEntries();
                addParamEntries(returnParamEntries, declarations);
            }
        }

        // Here we are in a parameter list. The only thing that adds scope in this context are the polymorphic
        // parameters
        if (containingScopeBlock instanceof OdinParamEntries paramEntries) {
            paramEntries.getParamEntryList().forEach(p -> declarations.add(p.getParameterDeclaration()));

            if (paramEntries.getParent() instanceof OdinReturnParameters returnParameters) {
                OdinProcedureType procedureType = PsiTreeUtil.getParentOfType(returnParameters, OdinProcedureType.class);
                if (procedureType != null) {
                    OdinParamEntries inParamEntries = procedureType.getParamEntries();
                    addParamEntries(inParamEntries, declarations);
                    addPolymorphicDeclarations(inParamEntries, declarations);
                }
            } else {
                addPolymorphicDeclarations(paramEntries, declarations);
            }
        }

        if (containingScopeBlock instanceof OdinForBlock forBlock) {
            addControlFlowInit(forBlock.getControlFlowInit(), declarations);
        }

        if (containingScopeBlock instanceof OdinForInBlock forInBlock) {
            declarations.addAll(forInBlock.getForInParameterDeclarationList());
        }

        if (containingScopeBlock instanceof OdinSwitchBlock switchBlock) {
            addControlFlowInit(switchBlock.getControlFlowInit(), declarations);
        }

        if (containingScopeBlock instanceof OdinSwitchInBlock switchInBlock) {
            declarations.add(switchInBlock.getSwitchTypeVariableDeclaration());
        }

        if (containingScopeBlock instanceof OdinUnionType unionType) {
            OdinParamEntries paramEntries = unionType.getParamEntries();
            addParamEntries(paramEntries, declarations);
        }

        if (containingScopeBlock instanceof OdinStructType structType) {
            OdinParamEntries paramEntries = structType.getParamEntries();
            addParamEntries(paramEntries, declarations);
        }

        return declarations;
    }

    private static void addPolymorphicDeclarations(OdinParamEntries paramEntries, List<OdinDeclaration> declarations) {
        if(paramEntries != null) {
            Collection<OdinPolymorphicType> polymorphicTypes = PsiTreeUtil.findChildrenOfType(paramEntries, OdinPolymorphicType.class);
            declarations.addAll(polymorphicTypes);
        }
    }

    private static void addParamEntries(OdinParamEntries paramEntries, List<OdinDeclaration> declarations) {
        if (paramEntries != null) {
            for (OdinParamEntry paramEntry : paramEntries.getParamEntryList()) {
                OdinDeclaration declaration = paramEntry.getParameterDeclaration();
                declarations.add(declaration);
            }
        }
    }

    private static void addControlFlowInit(@Nullable OdinControlFlowInit controlFlowInit, List<OdinDeclaration> declarations) {
        if (controlFlowInit != null && controlFlowInit.getStatement() instanceof OdinDeclaration declaration) {
            declarations.add(declaration);
        }
    }

    record PositionCheckResult(boolean validPosition, PsiElement commonParent, OdinDeclaration declaration) {

    }

    private static PositionCheckResult checkPosition(PsiElement position, @NotNull PsiElement declaredIdentifier, OdinDeclaration declaration) {

        // the position and the symbol MUST share a common parent
        PsiElement commonParent = PsiTreeUtil.findCommonParent(position, declaredIdentifier);
        if (commonParent == null) {
            return new PositionCheckResult(false, null, null);
        }

        // if the position is in the declaration itself, we can assume the identifier has not been really declared yet. skip
        // EXCEPT: If we are in a constant declaration, the declaration itself is in scope, however, it is only legal
        // to use in structs, and procedures. In union and constants using the declaration is not legal.
        boolean usageInsideDeclaration = declaration == commonParent || declaration == null;
        if (usageInsideDeclaration && !(declaration instanceof OdinConstantDeclaration)) {
            return new PositionCheckResult(false, commonParent, declaration);
        }

        // When the declaration is queried from above of where the declaration is in the tree,
        // by definition, we do not add the symbol
        boolean positionIsAboveDeclaration = PsiTreeUtil.isAncestor(position, declaration, false);
        if (positionIsAboveDeclaration)
            return new PositionCheckResult(false, commonParent, declaration);

        return new PositionCheckResult(true, commonParent, declaration);
    }
}
