package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.lang.psi.*;

import java.util.List;

public class OdinMinimalSymbolTableBuilder extends OdinSymbolTableBuilderBase {
    public OdinMinimalSymbolTableBuilder(PsiElement originalPosition,
                                         String packagePath,
                                         OdinSymbolTableBuilderListener listener,
                                         OdinContext initialContext) {
        super(originalPosition, packagePath, listener, initialContext);
    }

    public OdinMinimalSymbolTableBuilder(OdinContext context, PsiElement position) {
        super(context, position);
    }

    public static OdinMinimalSymbolTableBuilder create(OdinContext context, PsiElement position) {
        return new OdinMinimalSymbolTableBuilder(
                context,
                position
        );
    }

    @Override
    public OdinSymbolTable build() {
        OdinSymbolTable symbolTable = buildMinimalContext(this.originalPosition, false);

        return symbolTable == null ? OdinSymbolTable.EMPTY : symbolTable;
    }

    public OdinSymbolTable buildMinimalContext(PsiElement element, boolean constantsOnly) {
        OdinScopeBlock containingScopeBlock = getNextContainingScopeBlock(element);
        if (containingScopeBlock == null) {
            OdinSymbolTable rootContext = OdinSymbolTableHelper.getRootContext(element, packagePath);
            if (checkStopCondition(rootContext)) {
                return rootContext;
            }
            return null;
        }

        boolean constantsOnlyNext = isConstantsOnlyNext(constantsOnly, containingScopeBlock);
        boolean forceAddVar = isForceAddVar(containingScopeBlock);


        OdinSymbolTable symbolTable = new OdinSymbolTable(packagePath);

        // Add "offset" symbols first, i.e. the symbols available at the second argument of the builtin
        // procedure offset. These are the members of the type that is passed as first parameter.
        if (containingScopeBlock instanceof OdinArgument argument) {
            addOffsetOfSymbols(argument, symbolTable);
            if (checkStopCondition(symbolTable)) {
                return symbolTable;
            }
        }

        // Element initializers in compound literals
        if (containingScopeBlock instanceof OdinCompoundLiteral odinCompoundLiteral) {
            addSymbolsOfCompoundLiteral(element, odinCompoundLiteral, symbolTable);
            if (checkStopCondition(symbolTable)) {
                return symbolTable;
            }
        }

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        if (containingScopeBlock instanceof OdinProcedureDefinition) {
            addContextParameter(containingScopeBlock.getProject(), symbolTable);
            if (checkStopCondition(symbolTable)) {
                return symbolTable;
            }
        }

        boolean stopped = buildMinimalContextWithPredicate(constantsOnly, containingScopeBlock, symbolTable, forceAddVar);
        if (stopped) {
            return symbolTable;
        }

        if (listener.onBlockConsumed(symbolTable, containingScopeBlock))
            return symbolTable;

        OdinScopeBlock parentScopeBlock = PsiTreeUtil.getParentOfType(containingScopeBlock, false, OdinScopeBlock.class);
        return buildMinimalContext(parentScopeBlock, constantsOnlyNext);
    }

    protected boolean buildMinimalContextWithPredicate(boolean constantsOnly, OdinScopeBlock containingScopeBlock, OdinSymbolTable context, boolean forceAddVar) {
        if (containingScopeBlock instanceof OdinProcedureDefinition) {
            addContextParameter(containingScopeBlock.getProject(), context);
        }

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        List<OdinDeclaration> declarations = OdinSymbolTableHelper.getDeclarations(containingScopeBlock);
        for (OdinDeclaration declaration : declarations) {
            if (!(declaration instanceof OdinConstantDeclaration) && !isPolymorphicParameter(declaration) && !isStatic(declaration)) continue;
            OdinSymbolTableBuilderBase.PositionCheckResult positionCheckResult = checkPosition(declaration);
            if (!positionCheckResult.validPosition()) continue;

            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration, initialContext);

            context.addAll(localSymbols);

            if (checkStopCondition(context)) return true;
        }

        if (constantsOnly && !forceAddVar) return false;

        for (var declaration : declarations) {
            if (declaration instanceof OdinConstantDeclaration) continue;
            OdinSymbolTableBuilderBase.PositionCheckResult positionCheckResult = checkPosition(declaration);
            if (!positionCheckResult.validPosition()) continue;
            boolean shouldAdd = forceAddVar || isStrictlyBefore(declaration, positionCheckResult);

            if (!shouldAdd) continue;

            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration, initialContext);
            for (OdinSymbol symbol : localSymbols) {
                // Add stuff if we are in file scope (e.g. global variables)

                context.add(symbol);

                if (checkStopCondition(context)) return true;
            }
        }

        return false;
    }
}
