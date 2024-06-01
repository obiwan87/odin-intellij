package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
        // 2. Get the parent and define a flattened view of statements/expressions
        // 3. Check where the child is within that view
        // 4. Include all symbols previous to that child
        // 5. Set position to the parent and repeat recursively

        OdinScopeArea containingScopeBlock = PsiTreeUtil.getParentOfType(position, OdinScopeArea.class);

        if (containingScopeBlock == null)
            return OdinScope.EMPTY;

        if (containingScopeBlock instanceof OdinFileScope odinFileScope) {
            return OdinScopeResolver.getFileScopeDeclarations(odinFileScope);
        }

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        List<OdinSymbol> symbols = getSymbols(containingScopeBlock);

        // TODO Add constant symbols "::"

        OdinScope scope = new OdinScope();
        for (OdinSymbol symbol : symbols) {
            PositionCheckResult positionCheckResult = checkPosition(symbol, position);
            if (!positionCheckResult.validPosition)
                continue;

            OdinDeclaration declaration = positionCheckResult.declaration();

            if (declaration instanceof OdinConstantDeclaration constantDeclaration) {
                // TODO these could also be global symbols, in fact, we can generalize getFileScopeDeclarations to this
                List<OdinSymbol> localSymbols = OdinSymbolResolver.getLocalSymbols(constantDeclaration);
                scope.addAll(localSymbols);
            }
        }

        for (OdinSymbol symbol : symbols) {
            PositionCheckResult positionCheckResult = checkPosition(symbol, position);
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

        OdinScope parentScope = doFindVisibleSymbols(containingScopeBlock, scopeCondition);
        scope.setParentScope(parentScope);
        return scope;
    }

    private static @NotNull List<OdinSymbol> getSymbols(PsiElement containingScopeBlock) {
        List<OdinSymbol> symbols = new ArrayList<>();
        if (containingScopeBlock instanceof OdinStatementList statementList) {
            for (OdinStatement odinStatement : statementList.getStatementList()) {
                if (odinStatement instanceof OdinDeclaration declaration) {
                    symbols.addAll(OdinSymbolResolver.getLocalSymbols(declaration));
                }
            }
        }

        if (containingScopeBlock instanceof OdinIfBlock odinIfBlock) {
            addControlFlowInit(odinIfBlock.getControlFlowInit(), symbols);
        }

        if (containingScopeBlock instanceof OdinProcedureDefinition procedureDefinition) {
            OdinProcedureType procedureType = procedureDefinition.getProcedureType();
            List<OdinParamEntry> paramEntryList = procedureType.getParamEntryList();
            for (OdinParamEntry paramEntry : paramEntryList) {
                OdinDeclaration declaration = paramEntry.getParameterDeclaration();
                symbols.addAll(OdinSymbolResolver.getLocalSymbols(declaration));
            }

            if (procedureType.getReturnParameters() != null) {
                OdinParamEntries returnParamEntries = procedureType.getReturnParameters().getParamEntries();
                if (returnParamEntries != null) {
                    for (OdinParamEntry paramEntry : returnParamEntries.getParamEntryList()) {
                        OdinDeclaration declaration = paramEntry.getParameterDeclaration();
                        symbols.addAll(OdinSymbolResolver.getLocalSymbols(declaration));
                    }
                }
            }
        }

        // Here we are in a parameter list. The only thing that adds scope in this context are the polymorphic
        // parameters
        if (containingScopeBlock instanceof OdinParamEntries paramEntries) {
            addParamEntries(paramEntries, symbols);

            if (paramEntries.getParent() instanceof OdinReturnParameters returnParameters) {
                OdinProcedureType procedureType = PsiTreeUtil.getParentOfType(returnParameters, OdinProcedureType.class);
                if (procedureType != null) {
                    if (procedureType.getParamEntries() != null) {
                        addParamEntries(procedureType.getParamEntries(), symbols);
                    }
                }
            }
        }

        if (containingScopeBlock instanceof OdinForBlock forBlock) {
            addControlFlowInit(forBlock.getControlFlowInit(), symbols);
        }

        if(containingScopeBlock instanceof OdinForInBlock forInBlock) {
            forInBlock.getForInParameterList().stream().map(
                    p -> new OdinSymbol(p.getDeclaredIdentifier())
            ).forEach(symbols::add);
        }

        if(containingScopeBlock instanceof OdinSwitchBlock switchBlock) {
            addControlFlowInit(switchBlock.getControlFlowInit(), symbols);
        }

        if(containingScopeBlock instanceof OdinSwitchInBlock switchInBlock) {
            symbols.add(new OdinSymbol(switchInBlock.getDeclaredIdentifier()));
        }

        return symbols;
    }

    private static void addControlFlowInit(@Nullable OdinControlFlowInit controlFlowInit, List<OdinSymbol> symbols) {
        if (controlFlowInit != null && controlFlowInit.getStatement() instanceof OdinDeclaration declaration) {
            symbols.addAll(OdinSymbolResolver.getLocalSymbols(declaration));
        }
    }

    private static void addParamEntries(OdinParamEntries paramEntries, List<OdinSymbol> symbols) {
        List<OdinParamEntry> paramEntryList = paramEntries.getParamEntryList();
        for (OdinParamEntry paramEntry : paramEntryList) {
            Collection<OdinPolymorphicType> polymorphicTypes = PsiTreeUtil.findChildrenOfType(paramEntry, OdinPolymorphicType.class);
            Collection<OdinDeclaredIdentifier> polymorphicIdentifiers = PsiTreeUtil.findChildrenOfType(paramEntry, OdinDeclaredIdentifier.class)
                    .stream().filter(i -> i.getDollar() != null).toList();

            polymorphicIdentifiers.forEach(i -> symbols.add(new OdinSymbol(i)));
            polymorphicTypes.forEach(t -> symbols.add(new OdinSymbol(t.getDeclaredIdentifier())));
        }
    }


    record PositionCheckResult(boolean validPosition, PsiElement commonParent, OdinDeclaration declaration) {

    }

    private static PositionCheckResult checkPosition(OdinSymbol symbol, PsiElement position) {
        PsiNamedElement declaredIdentifier = symbol.getDeclaredIdentifier();

        // the position and the symbol MUST share a common parent
        PsiElement commonParent = PsiTreeUtil.findCommonParent(position, declaredIdentifier);
        if (commonParent == null) {
            return new PositionCheckResult(false, null, null);
        }

        OdinDeclaration declaration = PsiTreeUtil.getParentOfType(declaredIdentifier, OdinDeclaration.class);
        // if the position is in the declaration itself, we can assume the identifier has not been really declared yet. skip
        if (declaration == commonParent || declaration == null) {
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
