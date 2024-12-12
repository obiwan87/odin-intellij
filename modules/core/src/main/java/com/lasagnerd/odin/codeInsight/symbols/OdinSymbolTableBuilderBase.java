package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinAttributeUtils;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.typeInference.OdinExpectedTypeEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinStructType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public abstract class OdinSymbolTableBuilderBase implements OdinSymbolTableBuilder {
    protected final PsiElement originalPosition;
    protected final String packagePath;

    protected final OdinContext initialContext;
    protected final PsiElement typeExpectationContext;

    public static final OdinSymbolTableBuilderListener ALWAYS_FALSE = new OdinSymbolTableBuilderListener() {
    };

    @NotNull
    protected final OdinSymbolTableBuilderListener listener;


    public OdinSymbolTableBuilderBase(PsiElement originalPosition, String packagePath, OdinSymbolTableBuilderListener listener, OdinContext initialContext) {
        this.originalPosition = originalPosition;
        this.packagePath = packagePath;

        this.listener = listener;

        this.initialContext = initialContext;
        this.typeExpectationContext = OdinExpectedTypeEngine.findTypeExpectationContext(originalPosition);
    }

    public OdinSymbolTableBuilderBase(OdinContext context, PsiElement position) {
        this(position, OdinImportService.packagePath(position), ALWAYS_FALSE, context);
    }

    public abstract OdinSymbolTable build();

    protected void addSymbolsOfCompoundLiteral(PsiElement element, OdinCompoundLiteral containingScopeBlock, OdinSymbolTable context) {
        OdinLhs lhs = PsiTreeUtil.getParentOfType(element, OdinLhs.class, false);
        if (lhs != null) {
            TsOdinType tsOdinType = OdinInferenceEngine.inferTypeOfCompoundLiteral(initialContext, containingScopeBlock);
            List<OdinSymbol> elementSymbols = OdinInsightUtils.getElementSymbols(tsOdinType, tsOdinType.getContext());
            context.addAll(elementSymbols);
        }
    }

    protected static boolean isConstantsOnlyNext(boolean constantsOnly, OdinScopeBlock containingScopeBlock) {
        return containingScopeBlock instanceof OdinProcedureDefinition || constantsOnly;
    }

    protected static boolean isForceAddVar(OdinScopeBlock containingScopeBlock) {
        return containingScopeBlock instanceof OdinFileScope || containingScopeBlock instanceof OdinForeignBlock;
    }

    protected boolean checkStopCondition(OdinSymbolTable symbolTable) {
        return listener.onCheckpointCalled(symbolTable);
    }

    protected PositionCheckResult checkPosition(OdinDeclaration declaration) {
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
        if (positionIsAboveDeclaration) return new PositionCheckResult(false, commonParent, declaration);

        return new PositionCheckResult(true, commonParent, declaration);
    }

    protected static void addContextParameter(@NotNull Project project, OdinSymbolTable context) {
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
    protected static @Nullable OdinScopeBlock getNextContainingScopeBlock(PsiElement element) {
        OdinScopeBlock nextContainingScopeBlock = PsiTreeUtil.getParentOfType(element, true, OdinScopeBlock.class);
        if (nextContainingScopeBlock instanceof OdinSwitchInExpressionScope switchInExpressionScope) {

            nextContainingScopeBlock = PsiTreeUtil.getParentOfType(switchInExpressionScope, OdinScopeBlock.class);
            if (nextContainingScopeBlock != null) {
                nextContainingScopeBlock = PsiTreeUtil.getParentOfType(nextContainingScopeBlock, OdinScopeBlock.class);
            }
        }
        return nextContainingScopeBlock;
    }

    protected void addOffsetOfSymbols(OdinArgument argument, OdinSymbolTable symbolTable) {
        OdinCallExpression callExpression = PsiTreeUtil.getParentOfType(argument, OdinCallExpression.class);
        if (callExpression != null && callExpression.getArgumentList().size() == 2) {
            if (argument == callExpression.getArgumentList().get(1)) {
                OdinSymbol symbol = OdinInsightUtils.findBuiltinSymbolOfCallExpression(initialContext, callExpression, text -> text.equals("offset_of") || text.equals("offset_of_member"));
                if (symbol != null) {
                    OdinArgument odinArgument = callExpression.getArgumentList().getFirst();
                    OdinExpression typeExpression = getArgumentExpression(odinArgument);
                    if (typeExpression != null) {
                        TsOdinType tsOdinType = typeExpression.getInferredType();
                        if (tsOdinType instanceof TsOdinMetaType metaType) {
                            if (metaType.representedType() instanceof TsOdinStructType structType) {
                                OdinSymbolTable typeElements = OdinInsightUtils.getTypeElements(argument.getProject(), structType);
                                symbolTable.merge(typeElements);
                            }
                        }
                    }
                }
            }
        }


    }

    protected static @Nullable OdinExpression getArgumentExpression(OdinArgument odinArgument) {
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

    protected boolean isPolymorphicParameter(OdinDeclaration declaration) {
        if (declaration instanceof OdinPolymorphicType) return true;
        if (declaration instanceof OdinParameterDeclaration parameterDeclaration) {
            return parameterDeclaration.getDeclaredIdentifiers().stream().anyMatch(i -> i.getDollar() != null);
        }
        return false;
    }

    protected boolean isStatic(OdinDeclaration declaration) {
        if (declaration instanceof OdinVariableInitializationStatement variableInitializationStatement) {
            return OdinAttributeUtils.containsAttribute(variableInitializationStatement.getAttributesDefinitionList(), "static");
        }

        if (declaration instanceof OdinVariableDeclarationStatement variableDeclarationStatement) {
            return OdinAttributeUtils.containsAttribute(variableDeclarationStatement.getAttributesDefinitionList(), "static");
        }
        return false;
    }

    protected boolean isStrictlyBefore(OdinDeclaration declaration, PositionCheckResult positionCheckResult) {
        PsiElement commonParent = positionCheckResult.commonParent();
        PsiElement containerOfSymbol = declaration != commonParent ? PsiTreeUtil.findPrevParent(commonParent, declaration) : declaration;
        PsiElement containerOfPosition = originalPosition != commonParent ? PsiTreeUtil.findPrevParent(commonParent, originalPosition) : originalPosition;

        // Now check if symbol is strictly a previous sibling of position
        List<@NotNull PsiElement> childrenList = Arrays.stream(commonParent.getChildren()).toList();
        int indexOfSymbol = childrenList.indexOf(containerOfSymbol);
        int indexOfPosition = childrenList.indexOf(containerOfPosition);

        return indexOfPosition > indexOfSymbol;
    }

    protected record PositionCheckResult(boolean validPosition, PsiElement commonParent, OdinDeclaration declaration) {

    }
}
