package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.inferTypeInExplicitMode;
import static com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.inferTypeOfCompoundLiteral;
import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinTypeKind.*;

/**
 * Given an expression, computes what type is expected given the surrounding context.
 */
public class OdinExpectedTypeEngine {
    /**
     * Types are expected at in/out nodes such as return statements, case blocks, arguments, assignments of typed variables, etc.
     * This method finds the expected type of the RHS of where the passed PSI element is located in the AST.
     *
     * @param context The symbol table used for resolving the expected type
     * @param expression  The expression for which we want to find the expected type
     * @return The expected type
     */
    public static TsOdinType inferExpectedType(OdinContext context, OdinExpression expression) {
        // Finds the psi element that provides the context to compute the expected type
        PsiElement typeExpectationContext = findTypeExpectationContext(expression);
        if (typeExpectationContext == null)
            return TsOdinBuiltInTypes.UNKNOWN;

        // Find the top most expression under the rhs container
        PsiElement prevParent = typeExpectationContext != expression ? PsiTreeUtil.findPrevParent(typeExpectationContext, expression) : typeExpectationContext;
        if (!(prevParent instanceof OdinExpression topMostExpression)) {
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (typeExpectationContext instanceof OdinLhs) {
            OdinCompoundLiteral compoundLiteral = PsiTreeUtil.getParentOfType(typeExpectationContext, OdinCompoundLiteral.class);
            TsOdinType tsOdinType = inferTypeOfCompoundLiteral(context, compoundLiteral).baseType(true);
            if (tsOdinType instanceof TsOdinArrayType arrayType) {
                OdinExpression psiTypeExpression = arrayType.getPsiSizeElement().getExpression();
                if (psiTypeExpression != null) {
                    TsOdinType sizeType = psiTypeExpression.getInferredType(context);
                    if (sizeType instanceof TsOdinTypeReference typeReference) {
                        return typeReference.referencedType();
                    }
                }
            }
        }

        if (typeExpectationContext instanceof OdinReturnStatement returnStatement) {
            OdinProcedureDefinition procedureDefinition = PsiTreeUtil.getParentOfType(returnStatement, OdinProcedureDefinition.class);
            if (procedureDefinition != null) {
                int pos = returnStatement.getExpressionList().indexOf(topMostExpression);
                OdinReturnParameters returnParameters = procedureDefinition.getProcedureSignature().getProcedureType().getReturnParameters();
                if (returnParameters != null) {
                    OdinParamEntries paramEntries = returnParameters.getParamEntries();
                    if (paramEntries != null) {
                        if (paramEntries.getParamEntryList().size() > pos) {
                            OdinParamEntry paramEntry = paramEntries.getParamEntryList().get(pos);
                            return OdinTypeResolver.resolveType(context, paramEntry.getParameterDeclaration().getTypeDefinition());
                        }
                    } else if (pos == 0) {
                        OdinType psiType = returnParameters.getType();
                        if (psiType != null) {
                            return OdinTypeResolver.resolveType(context, psiType);
                        }
                    }
                }
            }
        }

        // Assignment operations
        if (typeExpectationContext instanceof OdinRhsExpressions rhsExpressions) {
            int index = rhsExpressions.getExpressionList().indexOf(topMostExpression);
            PsiElement grandParent = rhsExpressions.getParent();

            if (grandParent instanceof OdinAssignmentStatement statement) {

                List<OdinExpression> lhsExpressions = statement.getLhsExpressions().getExpressionList();
                if (lhsExpressions.size() > index) {
                    OdinExpression lhsExpression = lhsExpressions.get(index);
                    TsOdinType expectedType = lhsExpression.getInferredType(context);
                    return propagateTypeDown(expectedType, topMostExpression, expression);
                }
            }

            if (grandParent instanceof OdinInitVariableDeclaration initVariableDeclaration) {
                OdinType psiType = initVariableDeclaration.getType();
                if (psiType != null) {
                    TsOdinType tsOdinType = OdinTypeResolver.resolveType(context, psiType);
                    return propagateTypeDown(tsOdinType, topMostExpression, expression);
                }
            }
        }

        // Element entries
        if (typeExpectationContext instanceof OdinRhs) {
            OdinCompoundLiteral compoundLiteral = PsiTreeUtil.getParentOfType(typeExpectationContext, OdinCompoundLiteralTyped.class, OdinCompoundLiteralUntyped.class);
            Objects.requireNonNull(compoundLiteral);
            OdinElementEntry elemEntry = (OdinElementEntry) typeExpectationContext.getParent();

            TsOdinType tsOdinType = null;
            if (compoundLiteral instanceof OdinCompoundLiteralUntyped compoundLiteralUntyped) {
                OdinCompoundLiteralExpression parent = (OdinCompoundLiteralExpression) compoundLiteralUntyped.getParent();
                tsOdinType = parent.getInferredType(context);
            }

            if (compoundLiteral instanceof OdinCompoundLiteralTyped compoundLiteralTyped) {
                tsOdinType = OdinTypeResolver.resolveType(context, compoundLiteralTyped.getTypeContainer().getType());
            }

            if (tsOdinType != null) {
                TsOdinType tsOdinBaseType = tsOdinType.baseType(true);
                if (tsOdinBaseType instanceof TsOdinStructType tsOdinStructType) {

                    List<OdinSymbol> structFields = OdinInsightUtils.getStructFields(tsOdinStructType);
                    // Named element entry
                    if (elemEntry.getLhs() != null) {
                        String fieldName = elemEntry.getLhs().getText();
                        OdinSymbol symbol = structFields.stream().filter(s -> s.getName().equals(fieldName))
                                .findFirst()
                                .orElse(null);
                        if (symbol == null || symbol.getPsiType() == null) {
                            return TsOdinBuiltInTypes.UNKNOWN;
                        }
                        TsOdinType fieldType = OdinTypeResolver.resolveType(tsOdinType.getContext(), symbol.getPsiType());
                        return propagateTypeDown(fieldType, topMostExpression, expression);
                    }
                    // Positional initialization
                    else {
                        int index = compoundLiteral.getCompoundValue()
                                .getCompoundValueBody()
                                .getElementEntryList()
                                .indexOf(elemEntry);

                        if (structFields.size() > index) {
                            OdinSymbol symbol = structFields.get(index);
                            if (symbol == null || symbol.getPsiType() == null) {
                                return TsOdinBuiltInTypes.UNKNOWN;
                            }
                            TsOdinType fieldType = OdinTypeResolver.resolveType(tsOdinType.getContext(), symbol.getPsiType());
                            return propagateTypeDown(fieldType, topMostExpression, expression);
                        }
                        return TsOdinBuiltInTypes.UNKNOWN;
                    }
                } else if (tsOdinBaseType instanceof TsOdinArrayType tsOdinArrayType) {
                    return propagateTypeDown(tsOdinArrayType.getElementType(), topMostExpression, expression);
                } else if (tsOdinBaseType instanceof TsOdinSliceType tsOdinSliceType) {
                    return propagateTypeDown(tsOdinSliceType.getElementType(), topMostExpression, expression);
                } else if (tsOdinBaseType instanceof TsOdinDynamicArray tsOdinDynamicArray) {
                    return propagateTypeDown(tsOdinDynamicArray.getElementType(), topMostExpression, expression);
                } else if (tsOdinBaseType instanceof TsOdinBitSetType bitSetType) {
                    return propagateTypeDown(bitSetType.getElementType(), topMostExpression, expression);
                }
            }
        }

        if (typeExpectationContext instanceof OdinArgument argument) {
            OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(context, argument);

            if (!callInfo.callingType().isUnknown()) {
                TsOdinTypeKind typeReferenceKind = callInfo.callingType().getTypeReferenceKind();
                if (typeReferenceKind == ALIAS) {
                    typeReferenceKind = callInfo.callingType().baseType(true).getTypeReferenceKind();
                }

                if (typeReferenceKind == PROCEDURE || typeReferenceKind == PSEUDO_METHOD) {
                    TsOdinParameterOwner parameterOwner = (TsOdinParameterOwner) callInfo.callingType().baseType(true);
                    if (parameterOwner instanceof TsOdinProcedureType procedureType) {
                        parameterOwner = OdinTypeSpecializer.specializeProcedure(context, callInfo.argumentList(), procedureType);
                    }
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(parameterOwner.getParameters(), callInfo.argumentList());
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                        return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                    }
                } else if (typeReferenceKind == PROCEDURE_GROUP) {
                    TsOdinProcedureGroup callingProcedureGroup = (TsOdinProcedureGroup) callInfo.callingType().baseType(true);
                    OdinInferenceEngine.ProcedureRankingResult result = OdinProcedureRanker
                            .findBestProcedure(context, callingProcedureGroup, callInfo.argumentList());


                    if (result.bestProcedure() != null) {
                        TsOdinProcedureType specializedProcedure = OdinTypeSpecializer.specializeProcedure(
                                context,
                                callInfo.argumentList(),
                                result.bestProcedure()
                        );
                        Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(specializedProcedure.getParameters(), callInfo.argumentList());
                        if (argumentToParameterMap != null) {
                            TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                            return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                        }
                    } else {
                        boolean allUnnamed = callInfo.argumentList().stream().allMatch(a -> a instanceof OdinUnnamedArgument);
                        if (!allUnnamed)
                            return TsOdinBuiltInTypes.UNKNOWN;

                        int argumentIndex = callInfo.argumentList().indexOf(argument);
                        TsOdinType argumentType = null;
                        for (var entry : result.compatibleProcedures()) {
                            TsOdinProcedureType procedureType = entry.getFirst();
                            TsOdinProcedureType specializeProcedure = OdinTypeSpecializer.specializeProcedure(
                                    context,
                                    callInfo.argumentList(),
                                    procedureType
                            );
                            TsOdinType previousArgument = argumentType;
                            if (specializeProcedure.getParameters().size() > argumentIndex) {
                                argumentType = specializeProcedure.getParameters().get(argumentIndex).getType().baseType();
                            } else {
                                return TsOdinBuiltInTypes.UNKNOWN;
                            }

                            if (previousArgument != null) {
                                boolean argsCompatible = OdinTypeChecker.checkTypesStrictly(argumentType, previousArgument);
                                if (!argsCompatible) {
                                    return TsOdinBuiltInTypes.UNKNOWN;
                                }
                            }
                        }

                        return argumentType == null ? TsOdinBuiltInTypes.UNKNOWN : argumentType;
                    }
                } else if (typeReferenceKind == STRUCT) {
                    TsOdinStructType structType = (TsOdinStructType) callInfo.callingType().baseType(true);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(structType.getParameters(), callInfo.argumentList());
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                        return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                    }
                } else if (typeReferenceKind == UNION) {
                    TsOdinUnionType unionType = (TsOdinUnionType) callInfo.callingType().baseType(true);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(unionType.getParameters(), callInfo.argumentList());
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                        return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                    }
                } else if (callInfo.callingElement() instanceof OdinCallExpression odinCallExpression &&
                        (odinCallExpression.getExpression().parenthesesUnwrap() instanceof OdinRefExpression
                                || odinCallExpression.getExpression().parenthesesUnwrap() instanceof OdinTypeDefinitionExpression)) {
                    return propagateTypeDown(callInfo.callingType(), topMostExpression, expression);
                }
            }
        }

        if (typeExpectationContext instanceof OdinCaseClause caseClause) {
            OdinSwitchBlock switchBlock = PsiTreeUtil.getParentOfType(caseClause, OdinSwitchBlock.class);
            if (switchBlock != null) {
                OdinExpression switchBlockExpression = switchBlock.getExpression();
                if (switchBlockExpression == null) {
                    return TsOdinBuiltInTypes.UNKNOWN;
                }
                TsOdinType expectedType = switchBlockExpression.getInferredType(context);
                return propagateTypeDown(expectedType, topMostExpression, expression);
            }
        }

        if (typeExpectationContext instanceof OdinConstantInitDeclaration constantInitDeclaration) {
            OdinType type = constantInitDeclaration.getType();
            if (type != null) {
                return propagateTypeDown(OdinTypeResolver.resolveType(context, type), topMostExpression, expression);
            }
        }

        if (typeExpectationContext instanceof OdinInitVariableDeclaration initVariableDeclaration) {
            OdinType type = initVariableDeclaration.getType();
            if (type != null) {
                return propagateTypeDown(OdinTypeResolver.resolveType(context, type), topMostExpression, expression);
            }
        }

        if (typeExpectationContext instanceof OdinParameterInitialization parameterInitialization) {
            OdinType declaredType = parameterInitialization.getTypeDefinition();
            if (declaredType != null) {
                return propagateTypeDown(OdinTypeResolver.resolveType(context, declaredType), topMostExpression, expression);
            }
        }

        if (typeExpectationContext instanceof OdinIndex index && index.getParent() instanceof OdinIndexExpression indexExpression) {
            TsOdinType tsOdinType = OdinInferenceEngine.inferTypeInExplicitMode(context, indexExpression.getExpression());
            TsOdinType baseType = tsOdinType.baseType(true);
            if (baseType instanceof TsOdinArrayType tsOdinArrayType) {
                OdinArraySize psiSizeElement = tsOdinArrayType.getPsiSizeElement();
                if (psiSizeElement != null && psiSizeElement.getExpression() != null) {
                    TsOdinType expectedType = inferTypeInExplicitMode(context, psiSizeElement.getExpression());
                    if (expectedType instanceof TsOdinTypeReference tsOdinTypeReference) {
                        return propagateTypeDown(tsOdinTypeReference.referencedType(), topMostExpression, expression);
                    }
                    return TsOdinBuiltInTypes.UNKNOWN;
                }
            } else if (baseType instanceof TsOdinMapType tsOdinMapType) {
                return propagateTypeDown(tsOdinMapType.getKeyType(), topMostExpression, expression);
            }
        }

        return TsOdinBuiltInTypes.UNKNOWN;
    }

    public static TsOdinType propagateTypeDown(TsOdinType expectedType, OdinExpression topExpression, OdinExpression targetExpression) {
        if (topExpression == targetExpression)
            return expectedType;


        if (expectedType instanceof TsOdinPointerType pointerType && topExpression instanceof OdinAddressExpression addressExpression) {
            return propagateTypeDown(pointerType.getDereferencedType(), addressExpression.getExpression(), targetExpression);
        } else if (expectedType instanceof TsOdinArrayType tsOdinArrayType && topExpression instanceof OdinIndexExpression indexExpression) {
            return propagateTypeDown(tsOdinArrayType.getElementType(), indexExpression.getExpression(), targetExpression);
        } else if (topExpression instanceof OdinBinaryExpression binaryExpression) {
            OdinExpression nextExpression = OdinInsightUtils.findPrevParent(topExpression, targetExpression, false, OdinExpression.class);
            if (nextExpression == null)
                return TsOdinBuiltInTypes.UNKNOWN;

            if (binaryExpression.getLeft() == nextExpression || binaryExpression.getRight() == nextExpression) {
                return propagateTypeDown(expectedType, nextExpression, targetExpression);
            }
        } else if (topExpression instanceof OdinParenthesizedExpression parenthesizedExpression) {
            return propagateTypeDown(expectedType, parenthesizedExpression.getExpression(), targetExpression);
        } else if (topExpression instanceof OdinTernaryExpression ternaryExpression) {
            OdinExpression nextExpression = OdinInsightUtils.findPrevParent(topExpression, targetExpression, false, OdinExpression.class);
            if (nextExpression == null)
                return TsOdinBuiltInTypes.UNKNOWN;

            if (ternaryExpression.getExpressionList().contains(targetExpression)) {
                return propagateTypeDown(expectedType, nextExpression, targetExpression);
            }
        } else if (topExpression instanceof OdinUnaryExpression unaryExpression) {
            OdinExpression nextExpression = OdinInsightUtils.findPrevParent(topExpression, targetExpression, false, OdinExpression.class);
            if (nextExpression == null)
                return TsOdinBuiltInTypes.UNKNOWN;

            if (nextExpression == unaryExpression.getExpression()) {
                return propagateTypeDown(expectedType, nextExpression, targetExpression);
            }
        }

        // TODO add more
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    public static @Nullable PsiElement findTypeExpectationContext(PsiElement psiElement) {
        return OdinInsightUtils.findParentOfType(
                psiElement,
                false,
                new Class<?>[]{
                        OdinReturnStatement.class,
                        OdinRhs.class,
                        OdinLhs.class,
                        OdinArgument.class,
                        OdinRhsExpressions.class,
                        OdinCaseClause.class,
                        OdinInitVariableDeclaration.class,
                        OdinConstantInitDeclaration.class,
                        OdinParameterInitialization.class,
                        OdinIndex.class,
                        OdinCaseClause.class,
                },
                new Class<?>[]{
                        OdinLhsExpressions.class,
                        OdinLhs.class,
                }
        );
    }

}
