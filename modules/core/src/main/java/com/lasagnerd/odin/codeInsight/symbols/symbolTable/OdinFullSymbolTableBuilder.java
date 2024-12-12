package com.lasagnerd.odin.codeInsight.symbols.symbolTable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinDeclarationSymbolResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class OdinFullSymbolTableBuilder extends OdinSymbolTableBuilderBase {
    private @Nullable OdinCompoundLiteral parentCompoundLiteral;

    public OdinFullSymbolTableBuilder(OdinContext context, PsiElement position) {
        super(context, position);
    }

    public OdinFullSymbolTableBuilder(PsiElement originalPosition,
                                      String packagePath,
                                      OdinSymbolTableBuilderListener listener,
                                      OdinContext initialContext) {
        super(originalPosition, packagePath, listener, initialContext);
    }

    @Override
    public OdinSymbolTable build() {
        return buildFullContext();
    }

    public OdinSymbolTable buildFullContext() {
        OdinSymbolTable fullContext = buildFullContext(originalPosition);
        return trimToPosition(fullContext, false);
    }

    protected OdinSymbolTable buildFullContext(PsiElement element) {
        // 1. Find the starting point
        //  = a statement whose parent is a scope block
        // 2. Get the parent and get all declarations inside the scope block
        // 3. Add all constant declarations as they are not dependent on the position within the scope block
        // 4. Add all non-constant declarations, depending on whether the position is before or after
        //    the declared symbol

        OdinScopeBlock containingScopeBlock = getNextContainingScopeBlock(element);

        if (containingScopeBlock == null) {
            return Objects.requireNonNullElseGet(OdinSymbolTableHelper.getRootSymbolTable(element, this.packagePath), () -> new OdinSymbolTable(packagePath));
        }

        if (containingScopeBlock.getFullSymbolTable() != null) {
            // re-using symbol table
            OdinSymbolTable fullSymbolTable = containingScopeBlock.getFullSymbolTable();
            OdinSymbolTable parentContext = buildFullContext(containingScopeBlock);
            fullSymbolTable.setParentSymbolTable(parentContext);
            return fullSymbolTable;
        }

        OdinSymbolTable parentContext = buildFullContext(containingScopeBlock);

        return doBuildFullContext(containingScopeBlock, parentContext);
    }

    protected @NotNull OdinSymbolTable doBuildFullContext(OdinScopeBlock containingScopeBlock, OdinSymbolTable parentContext) {
        OdinSymbolTable symbolTable = new OdinSymbolTable(packagePath);
        symbolTable.setScopeBlock(containingScopeBlock);
        containingScopeBlock.setFullSymbolTable(symbolTable);

        symbolTable.setParentSymbolTable(parentContext);

        // Bring field declarations and swizzle into scope
        if (containingScopeBlock instanceof OdinCompoundLiteral compoundLiteral) {
            TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(initialContext, compoundLiteral);
            List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, tsOdinType.getContext());
            symbolTable.addAll(elementSymbols);
        }


        if (containingScopeBlock instanceof OdinArgument argument) {
            addOffsetOfSymbols(argument, symbolTable);
        }

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        if (containingScopeBlock instanceof OdinProcedureDefinition) {
            addContextParameter(containingScopeBlock.getProject(), symbolTable);
        }
        List<OdinDeclaration> declarations = OdinSymbolTableHelper.getDeclarations(containingScopeBlock);
        for (OdinDeclaration declaration : declarations) {
            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration, initialContext);

            symbolTable.getDeclarationSymbols().computeIfAbsent(declaration, d -> new ArrayList<>()).addAll(localSymbols);

            symbolTable.addAll(localSymbols);
        }

        return symbolTable;
    }

    protected Collection<OdinSymbol> externalSymbols(OdinSymbolTable OdinSymbolTable) {
        // TODO causes concurrent modification exception occasionally
        Set<OdinSymbol> declarationSymbols = OdinSymbolTable.getDeclarationSymbols().values().stream().flatMap(List::stream).collect(Collectors.toSet());
        HashSet<OdinSymbol> externalSymbols = new HashSet<>(OdinSymbolTable.getSymbolTable().values().stream().flatMap(List::stream).toList());
        externalSymbols.removeAll(declarationSymbols);

        return externalSymbols;
    }

    protected OdinSymbolTable trimToPosition(OdinSymbolTable symbolTable, boolean constantsOnly) {
        // 1. Find the starting point
        //  = a statement whose parent is a scope block
        // 2. Get the parent and get all declarations inside the scope block
        // 3. Add all constant declarations as they are not dependent on the position within the scope block
        // 4. Add all non-constant declarations, depending on whether the position is before or after
        //    the declared symbol
        if (symbolTable == null) return null;
        OdinScopeBlock containingScopeBlock = symbolTable.getScopeBlock();

        boolean fileScope = containingScopeBlock instanceof OdinFileScope;
        boolean foreignBlock = containingScopeBlock instanceof OdinForeignBlock;

        if (containingScopeBlock == null) return symbolTable;

        boolean constantsOnlyNext = isConstantsOnlyNext(constantsOnly, containingScopeBlock);

        if (containingScopeBlock instanceof OdinCompoundLiteral) {
            if (typeExpectationContext instanceof OdinLhs) {
                this.parentCompoundLiteral = this.parentCompoundLiteral == null ? PsiTreeUtil.getParentOfType(typeExpectationContext, OdinCompoundLiteral.class) : this.parentCompoundLiteral;
                if (parentCompoundLiteral != containingScopeBlock) {
                    return trimToPosition(symbolTable.getParentSymbolTable(), constantsOnlyNext);
                }
            } else {
                return trimToPosition(symbolTable.getParentSymbolTable(), constantsOnlyNext);
            }
        }

        OdinSymbolTable context = new OdinSymbolTable(packagePath);
        context.setScopeBlock(containingScopeBlock);

        // Since odin does not support closures, all symbols above the current scope, are visible only if they are constants
        OdinSymbolTable nextParentContext = symbolTable.getParentSymbolTable();

        OdinSymbolTable trimmedParentContext = trimToPosition(nextParentContext, constantsOnlyNext);

        context.setParentSymbolTable(trimmedParentContext);
        context.addAll(externalSymbols(symbolTable));

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        Set<OdinDeclaration> declarations = symbolTable.getDeclarationSymbols().keySet();
        // TODO causes concurrent modification exception occasionally
        for (OdinDeclaration declaration : declarations) {
            if (!(declaration instanceof OdinConstantDeclaration) && !isPolymorphicParameter(declaration) && !isStatic(declaration)) continue;

            PositionCheckResult positionCheckResult;
            positionCheckResult = checkPosition(declaration);

            if (!positionCheckResult.validPosition()) continue;

            List<OdinSymbol> localSymbols = symbolTable.getDeclarationSymbols(declaration);
            context.addAll(localSymbols);

            if (checkStopCondition(context)) return context;
        }


        if (constantsOnly && !fileScope && !foreignBlock) return context;

        for (var declaration : declarations) {
            if (declaration instanceof OdinConstantDeclaration) continue;
            List<OdinSymbol> localSymbols = symbolTable.getDeclarationSymbols(declaration);
            for (OdinSymbol symbol : localSymbols) {
                PositionCheckResult positionCheckResult = checkPosition(declaration);
                if (!positionCheckResult.validPosition()) continue;


                // Add stuff if we are in file scope (e.g. global variables)
                boolean shouldAdd = fileScope || foreignBlock || isStrictlyBefore(declaration, positionCheckResult);

                if (shouldAdd) {
                    context.add(symbol);
                }

                if (checkStopCondition(context)) return context;
            }
        }

        return context;

    }
}
