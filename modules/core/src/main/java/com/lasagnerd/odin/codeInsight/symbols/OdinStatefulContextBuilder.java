package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinExpectedTypeEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinStructType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class OdinStatefulContextBuilder {
    static final OdinContextBuilder.StopCondition ALWAYS_FALSE = context -> false;
    private final PsiElement originalPosition;
    private final String packagePath;
    private final OdinContextBuilder.StopCondition stopCondition;
    private final OdinContext initialContext;
    private final PsiElement psiContext;
    private @Nullable OdinCompoundLiteral parentCompoundLiteral;

    public OdinStatefulContextBuilder(PsiElement originalPosition,
                                      String packagePath,
                                      OdinContextBuilder.StopCondition stopCondition,
                                      OdinContext initialContext) {
        this.originalPosition = originalPosition;
        this.packagePath = packagePath;
        this.stopCondition = stopCondition;
        this.initialContext = initialContext;
        this.psiContext = OdinExpectedTypeEngine.findTypeExpectationContext(originalPosition);
    }

    public OdinContext buildFullContext() {
        OdinContext fullContext = buildFullContext(originalPosition);
        return trimToPosition(fullContext, false);
    }

    public OdinContext buildMinimalContext(PsiElement element, boolean constantsOnly) {
        OdinScopeBlock containingScopeBlock = getNextContainingScopeBlock(element);
        if (containingScopeBlock == null) {
            OdinContext rootContext = OdinContextBuilder.getRootContext(element, packagePath);
            if (checkStopCondition(rootContext)) {
                return rootContext;
            }
            return null;
        }

        boolean constantsOnlyNext = isConstantsOnlyNext(constantsOnly, containingScopeBlock);
        boolean forceAddVar = isForceAddVar(containingScopeBlock);


        OdinContext context = new OdinContext(packagePath);

        // Add "offset" symbols first, i.e. the symbols available at the second argument of the builtin
        // procedure offset. These are the members of the type that is passed as first parameter.
        if (containingScopeBlock instanceof OdinArgument argument) {
            addOffsetOfSymbols(argument, context);
            if (checkStopCondition(context)) {
                return context;
            }
        }

        // Element initializers in compound literals
        if (containingScopeBlock instanceof OdinCompoundLiteral odinCompoundLiteral) {
            addSymbolsOfCompoundLiteral(element, odinCompoundLiteral, context);
            if (checkStopCondition(context)) {
                return context;
            }
        }

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        if (containingScopeBlock instanceof OdinProcedureDefinition) {
            addContextParameter(containingScopeBlock.getProject(), context);
            if (checkStopCondition(context)) {
                return context;
            }
        }

        boolean stopped = buildContextWithPredicate(constantsOnly, containingScopeBlock, context, forceAddVar);
        if (stopped) {
            return context;
        }

        OdinScopeBlock parentScopeBlock = PsiTreeUtil.getParentOfType(containingScopeBlock, false, OdinScopeBlock.class);
        return buildMinimalContext(parentScopeBlock, constantsOnlyNext);
    }

    // Bring field declarations and swizzle into scope
    private static void addSymbolsOfCompoundLiteral(PsiElement element, OdinCompoundLiteral containingScopeBlock, OdinContext context) {
        OdinLhs lhs = PsiTreeUtil.getParentOfType(element, OdinLhs.class, false);
        if (lhs != null) {
            TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(context, containingScopeBlock);
            List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, tsOdinType.getContext());
            context.addAll(elementSymbols);
        }
    }

    private OdinContext buildFullContext(PsiElement element) {
        // 1. Find the starting point
        //  = a statement whose parent is a scope block
        // 2. Get the parent and get all declarations inside the scope block
        // 3. Add all constant declarations as they are not dependent on the position within the scope block
        // 4. Add all non-constant declarations, depending on whether the position is before or after
        //    the declared symbol

        OdinScopeBlock containingScopeBlock = getNextContainingScopeBlock(element);

        if (containingScopeBlock == null) {
            return Objects.requireNonNullElseGet(OdinContextBuilder.getRootContext(element, this.packagePath), () -> new OdinContext(packagePath));
        }

        if (containingScopeBlock.getFullContext() != null) {
            // re-using symbol table
            OdinContext context = containingScopeBlock.getFullContext();
            OdinContext parentContext = buildFullContext(containingScopeBlock);
            context.setParentContext(parentContext);
            return context;
        }

        OdinContext parentContext = buildFullContext(containingScopeBlock);

        return doBuildFullContext(containingScopeBlock, parentContext);
    }

    private @NotNull OdinContext doBuildFullContext(OdinScopeBlock containingScopeBlock, OdinContext parentContext) {
        OdinContext context = new OdinContext(packagePath);
        context.setScopeBlock(containingScopeBlock);
        containingScopeBlock.setFullContext(context);

        context.setParentContext(parentContext);

        // Bring field declarations and swizzle into scope
        if (containingScopeBlock instanceof OdinCompoundLiteral compoundLiteral) {
            TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(context, compoundLiteral);
            List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, tsOdinType.getContext());
            context.addAll(elementSymbols);
        }


        if (containingScopeBlock instanceof OdinArgument argument) {
            addOffsetOfSymbols(argument, context);
        }

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        if (containingScopeBlock instanceof OdinProcedureDefinition) {
            addContextParameter(containingScopeBlock.getProject(), context);
        }
        List<OdinDeclaration> declarations = OdinContextBuilder.getDeclarations(containingScopeBlock);
        for (OdinDeclaration declaration : declarations) {
            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration, context);

            context.getDeclarationSymbols()
                    .computeIfAbsent(declaration, d -> new ArrayList<>())
                    .addAll(localSymbols);

            context.addAll(localSymbols);
        }

        return context;
    }

    private Collection<OdinSymbol> externalSymbols(OdinContext odinContext) {
        // TODO causes concurrent modification exception occasionally
        Set<OdinSymbol> declarationSymbols = odinContext.getDeclarationSymbols().values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        HashSet<OdinSymbol> externalSymbols = new HashSet<>(odinContext.getSymbolTable().values().stream()
                .flatMap(List::stream).toList());
        externalSymbols.removeAll(declarationSymbols);

        return externalSymbols;
    }

    private OdinContext trimToPosition(OdinContext fullContext, boolean constantsOnly) {
        // 1. Find the starting point
        //  = a statement whose parent is a scope block
        // 2. Get the parent and get all declarations inside the scope block
        // 3. Add all constant declarations as they are not dependent on the position within the scope block
        // 4. Add all non-constant declarations, depending on whether the position is before or after
        //    the declared symbol
        if (fullContext == null)
            return null;
        OdinScopeBlock containingScopeBlock = fullContext.getScopeBlock();

        boolean fileScope = containingScopeBlock instanceof OdinFileScope;
        boolean foreignBlock = containingScopeBlock instanceof OdinForeignBlock;

        if (containingScopeBlock == null)
            return fullContext;

        boolean constantsOnlyNext = isConstantsOnlyNext(constantsOnly, containingScopeBlock);

        if (containingScopeBlock instanceof OdinCompoundLiteral) {
            if (psiContext instanceof OdinLhs) {
                this.parentCompoundLiteral = this.parentCompoundLiteral == null ? PsiTreeUtil.getParentOfType(psiContext, OdinCompoundLiteral.class)
                        : this.parentCompoundLiteral;
                if (parentCompoundLiteral != containingScopeBlock) {
                    return trimToPosition(fullContext.getParentContext(), constantsOnlyNext);
                }
            } else {
                return trimToPosition(fullContext.getParentContext(), constantsOnlyNext);
            }
        }

        OdinContext context = new OdinContext(packagePath);
        context.setScopeBlock(containingScopeBlock);

        // Since odin does not support closures, all symbols above the current scope, are visible only if they are constants
        OdinContext nextParentContext = fullContext.getParentContext();

        OdinContext trimmedParentContext = trimToPosition(nextParentContext, constantsOnlyNext);

        context.setParentContext(trimmedParentContext);
        context.addAll(externalSymbols(fullContext));

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        Set<OdinDeclaration> declarations = fullContext.getDeclarationSymbols().keySet();
        // TODO causes concurrent modification exception occasionally
        for (OdinDeclaration declaration : declarations) {
            if (!(declaration instanceof OdinConstantDeclaration)
                    && !isPolymorphicParameter(declaration)
                    && !isStatic(declaration))
                continue;

            PositionCheckResult positionCheckResult;
            positionCheckResult = checkPosition(declaration);

            if (!positionCheckResult.validPosition)
                continue;

            List<OdinSymbol> localSymbols = fullContext.getDeclarationSymbols(declaration);
            context.addAll(localSymbols);

            if (checkStopCondition(context))
                return context;
        }


        if (constantsOnly && !fileScope && !foreignBlock)
            return context;

        for (var declaration : declarations) {
            if (declaration instanceof OdinConstantDeclaration)
                continue;
            List<OdinSymbol> localSymbols = fullContext.getDeclarationSymbols(declaration);
            for (OdinSymbol symbol : localSymbols) {
                PositionCheckResult positionCheckResult = checkPosition(declaration);
                if (!positionCheckResult.validPosition)
                    continue;


                // Add stuff if we are in file scope (e.g. global variables)
                boolean shouldAdd = fileScope
                        || foreignBlock
                        || isStrictlyBefore(declaration, positionCheckResult);

                if (shouldAdd) {
                    context.add(symbol);
                }

                if (checkStopCondition(context))
                    return context;
            }
        }

        return context;

    }

    private static boolean isConstantsOnlyNext(boolean constantsOnly, OdinScopeBlock containingScopeBlock) {
        return containingScopeBlock instanceof OdinProcedureDefinition || constantsOnly;
    }

    private static boolean isForceAddVar(OdinScopeBlock containingScopeBlock) {
        return containingScopeBlock instanceof OdinFileScope
                || containingScopeBlock instanceof OdinForeignBlock;
    }

    private boolean buildContextWithPredicate(boolean constantsOnly,
                                              OdinScopeBlock containingScopeBlock,
                                              OdinContext context,
                                              boolean forceAddVar) {
        if (containingScopeBlock instanceof OdinProcedureDefinition) {
            addContextParameter(containingScopeBlock.getProject(), context);
        }

        // Finds the child of the scope block, where of which this element is a child. If we find the parent, this is guaranteed
        // to be != null
        List<OdinDeclaration> declarations = OdinContextBuilder.getDeclarations(containingScopeBlock);
        for (OdinDeclaration declaration : declarations) {
            if (!(declaration instanceof OdinConstantDeclaration) && !isPolymorphicParameter(declaration) && !isStatic(declaration))
                continue;
            PositionCheckResult positionCheckResult = checkPosition(declaration);
            if (!positionCheckResult.validPosition)
                continue;

            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration, initialContext);

            context.addAll(localSymbols);

            if (checkStopCondition(context))
                return true;
        }

        if (constantsOnly && !forceAddVar)
            return false;

        for (var declaration : declarations) {
            if (declaration instanceof OdinConstantDeclaration)
                continue;
            PositionCheckResult positionCheckResult = checkPosition(declaration);
            if (!positionCheckResult.validPosition)
                continue;
            boolean shouldAdd = forceAddVar
                    || isStrictlyBefore(declaration, positionCheckResult);

            if (!shouldAdd)
                continue;

            List<OdinSymbol> localSymbols = OdinDeclarationSymbolResolver.getSymbols(declaration, initialContext);
            for (OdinSymbol symbol : localSymbols) {
                // Add stuff if we are in file scope (e.g. global variables)

                context.add(symbol);

                if (checkStopCondition(context))
                    return true;
            }
        }

        return false;
    }

    private boolean checkStopCondition(OdinContext context) {
        return stopCondition != ALWAYS_FALSE && stopCondition.match(context);
    }

    private PositionCheckResult checkPosition(OdinDeclaration declaration) {
        // the position and the symbol MUST share a common parent
        PsiElement commonParent = PsiTreeUtil.findCommonParent(originalPosition, declaration);
        if (commonParent == null) {
            return new PositionCheckResult(false, null, null);
        }


        // if the position is in the declaration itself, we can assume the identifier has not been really declared yet. skip
        // EXCEPT: If we are in a constant declaration, the declaration itself is in scope, however, it is only legal
        // to use in structs, and procedures. In union and constants using the declaration is not legal.
        boolean usageInsideDeclaration = declaration == commonParent;
        if (usageInsideDeclaration) {
            OdinType type = OdinInsightUtils.getDeclaredType(declaration);
            OdinProcedureDefinition procedureDefinition;
            if (type instanceof OdinProcedureType) {
                procedureDefinition = PsiTreeUtil.getParentOfType(type, OdinProcedureDefinition.class);
            } else if (type instanceof OdinProcedureLiteralType procedureLiteralType) {
                procedureDefinition = procedureLiteralType.getProcedureDefinition();
            } else {
                procedureDefinition = null;
            }

            if (procedureDefinition != null) {
                OdinProcedureBody declarationBody = procedureDefinition.getProcedureBody();
                OdinProcedureBody procedureBody = PsiTreeUtil.getParentOfType(originalPosition, OdinProcedureBody.class, false);

                if (procedureBody != null && PsiTreeUtil.isAncestor(declarationBody, procedureBody, false)) {
                    return new PositionCheckResult(true, commonParent, declaration);
                }
            }


            if (type instanceof OdinStructType structType) {
                OdinStructBlock declarationStructBlock = structType.getStructBlock();
                OdinStructBlock structBlock = PsiTreeUtil.getParentOfType(originalPosition, OdinStructBlock.class);

                if (structBlock != null && PsiTreeUtil.isAncestor(declarationStructBlock, structBlock, false)) {
                    return new PositionCheckResult(true, commonParent, declaration);
                }
            }

            return new PositionCheckResult(false, commonParent, declaration);
        }

        // Within param entries, polymorphic parameters and other constant declaration are not visible
        // from earlier parameters
        if (commonParent instanceof OdinParamEntries paramEntries) {
            OdinParamEntry paramEntryPosition = (OdinParamEntry) PsiTreeUtil.findPrevParent(commonParent, originalPosition);
            OdinParamEntry paramEntryDeclaration = (OdinParamEntry) PsiTreeUtil.findPrevParent(commonParent, declaration);

            int indexPosition = paramEntries.getParamEntryList().indexOf(paramEntryPosition);
            int indexDeclaration = paramEntries.getParamEntryList().indexOf(paramEntryDeclaration);
            if (indexPosition < indexDeclaration) {
                return new PositionCheckResult(false, commonParent, declaration);
            }
        }
        // When the declaration is queried from above of where the declaration is in the tree,
        // by definition, we do not add the symbol
        boolean positionIsAboveDeclaration = PsiTreeUtil.isAncestor(originalPosition, declaration, false);
        if (positionIsAboveDeclaration)
            return new PositionCheckResult(false, commonParent, declaration);

        return new PositionCheckResult(true, commonParent, declaration);
    }

    private static void addContextParameter(@NotNull Project project, OdinContext context) {
        OdinSdkService builtinSymbolService = OdinSdkService.getInstance(project);
        if (builtinSymbolService != null) {
            // TODO check logic of "contextless"
            //OdinStringLiteral callConvention = procedureDefinition.getProcedureType().getStringLiteral();
            //                    String stringLiteralValue = OdinInsightUtils.getStringLiteralValue(callConvention);
            //                    if (stringLiteralValue == null && ) {
            //                        context.add(builtinSymbolService.createNewContextParameterSymbol());
            //                    }
            context.add(OdinSdkService.createContextSymbol(project));
        }
    }

    // In the AST the expression in "switch v in expr" is within the switch scope area, however,
    // semantically the variable "v" does not belong in the scope of the expression. Hence, we skip
    // it
    private static @Nullable OdinScopeBlock getNextContainingScopeBlock(PsiElement element) {
        OdinScopeBlock nextContainingScopeBlock = PsiTreeUtil.getParentOfType(element, true, OdinScopeBlock.class);
        if (nextContainingScopeBlock instanceof OdinSwitchInExpressionScope switchInExpressionScope) {

            nextContainingScopeBlock = PsiTreeUtil.getParentOfType(switchInExpressionScope, OdinScopeBlock.class);
            if (nextContainingScopeBlock != null) {
                nextContainingScopeBlock = PsiTreeUtil.getParentOfType(nextContainingScopeBlock, OdinScopeBlock.class);
            }
        }
        return nextContainingScopeBlock;
    }

    private static void addOffsetOfSymbols(OdinArgument argument, OdinContext context) {
        OdinCallExpression callExpression = PsiTreeUtil.getParentOfType(argument, OdinCallExpression.class);
        if (callExpression != null && callExpression.getArgumentList().size() == 2) {
            if (argument == callExpression.getArgumentList().get(1)) {
                OdinSymbol symbol = OdinInsightUtils.findBuiltinSymbolOfCallExpression(context,
                        callExpression,
                        text -> text.equals("offset_of") || text.equals("offset_of_member"));
                if (symbol != null) {
                    OdinArgument odinArgument = callExpression.getArgumentList().getFirst();
                    OdinExpression typeExpression = getArgumentExpression(odinArgument);
                    if (typeExpression != null) {
                        TsOdinType tsOdinType = typeExpression.getInferredType();
                        if (tsOdinType instanceof TsOdinMetaType metaType) {
                            if (metaType.representedType() instanceof TsOdinStructType structType) {
                                OdinContext typeElements = OdinInsightUtils.getTypeElements(argument.getProject(), structType);
                                context.putAll(typeElements);
                            }
                        }
                    }
                }
            }
        }


    }

    private static @Nullable OdinExpression getArgumentExpression(OdinArgument odinArgument) {
        OdinExpression typeExpression;
        if (odinArgument instanceof OdinUnnamedArgument typeArgument) {
            typeExpression = typeArgument.getExpression();
        } else if (odinArgument instanceof OdinNamedArgument namedTypeArgument) {
            typeExpression = namedTypeArgument.getExpression();
        } else {
            typeExpression = null;
        }
        return typeExpression;
    }

    private boolean isPolymorphicParameter(OdinDeclaration declaration) {
        if (declaration instanceof OdinPolymorphicType)
            return true;
        if (declaration instanceof OdinParameterDeclaration parameterDeclaration) {
            return parameterDeclaration.getDeclaredIdentifiers().stream().anyMatch(i -> i.getDollar() != null);
        }
        return false;
    }

    private boolean isStatic(OdinDeclaration declaration) {
        if (declaration instanceof OdinVariableInitializationStatement variableInitializationStatement) {
            return OdinAttributeUtils.containsAttribute(variableInitializationStatement.getAttributesDefinitionList(), "static");
        }

        if (declaration instanceof OdinVariableDeclarationStatement variableDeclarationStatement) {
            return OdinAttributeUtils.containsAttribute(variableDeclarationStatement.getAttributesDefinitionList(), "static");
        }
        return false;
    }

    private boolean isStrictlyBefore(OdinDeclaration declaration, PositionCheckResult positionCheckResult) {
        PsiElement commonParent = positionCheckResult.commonParent();
        PsiElement containerOfSymbol = declaration != commonParent ? PsiTreeUtil.findPrevParent(commonParent, declaration) : declaration;
        PsiElement containerOfPosition = originalPosition != commonParent ? PsiTreeUtil.findPrevParent(commonParent, originalPosition) : originalPosition;

        // Now check if symbol is strictly a previous sibling of position
        List<@NotNull PsiElement> childrenList = Arrays.stream(commonParent.getChildren()).toList();
        int indexOfSymbol = childrenList.indexOf(containerOfSymbol);
        int indexOfPosition = childrenList.indexOf(containerOfPosition);

        return indexOfPosition > indexOfSymbol;
    }

    record PositionCheckResult(boolean validPosition, PsiElement commonParent, OdinDeclaration declaration) {

    }
}
