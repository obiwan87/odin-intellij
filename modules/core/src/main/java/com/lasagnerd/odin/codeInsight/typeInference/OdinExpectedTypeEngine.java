package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.inferTypeInExplicitMode;
import static com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine.inferTypeOfCompoundLiteral;
import static com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType.MetaType.*;

/**
 * Given an expression, computes what type is expected given the surrounding context.
 */
public class OdinExpectedTypeEngine {
    /**
     * Types are expected at in/out nodes such as return statements, case blocks, arguments, assignments of typed variables, etc.
     * This method finds the expected type of the RHS of where the passed PSI element is located in the AST.
     *
     * @param symbolTable The symbol table used for resolving the expected type
     * @param expression  The expression for which we want to find the expected type
     * @return The expected type
     */
    public static TsOdinType inferExpectedType(OdinSymbolTable symbolTable, OdinExpression expression) {
        // Finds the psi element that provides the context to compute the expected type
        PsiElement context = findTypeExpectationContext(expression);
        if (context == null)
            return TsOdinBuiltInTypes.UNKNOWN;

        // Find the top most expression under the rhs container
        PsiElement prevParent = context != expression ? PsiTreeUtil.findPrevParent(context, expression) : context;
        if (!(prevParent instanceof OdinExpression topMostExpression)) {
            return TsOdinBuiltInTypes.UNKNOWN;
        }

        if (context instanceof OdinLhs) {
            OdinCompoundLiteral compoundLiteral = PsiTreeUtil.getParentOfType(context, OdinCompoundLiteral.class);
            TsOdinType tsOdinType = inferTypeOfCompoundLiteral(symbolTable, compoundLiteral).baseType(true);
            if (tsOdinType instanceof TsOdinArrayType arrayType) {
                OdinExpression psiTypeExpression = arrayType.getPsiSizeElement().getExpression();
                if (psiTypeExpression != null) {
                    TsOdinType sizeType = psiTypeExpression.getInferredType(symbolTable);
                    if (sizeType instanceof TsOdinMetaType metaType) {
                        return metaType.representedType();
                    }
                }
            }
        }

        if (context instanceof OdinReturnStatement returnStatement) {
            OdinProcedureDefinition procedureDefinition = PsiTreeUtil.getParentOfType(returnStatement, OdinProcedureDefinition.class);
            if (procedureDefinition != null) {
                int pos = returnStatement.getExpressionList().indexOf(topMostExpression);
                OdinReturnParameters returnParameters = procedureDefinition.getProcedureSignature().getProcedureType().getReturnParameters();
                if (returnParameters != null) {
                    OdinParamEntries paramEntries = returnParameters.getParamEntries();
                    if (paramEntries != null) {
                        if (paramEntries.getParamEntryList().size() > pos) {
                            OdinParamEntry paramEntry = paramEntries.getParamEntryList().get(pos);
                            return OdinTypeResolver.resolveType(symbolTable, paramEntry.getParameterDeclaration().getTypeDefinition());
                        }
                    } else if (pos == 0) {
                        OdinType psiType = returnParameters.getType();
                        if (psiType != null) {
                            return OdinTypeResolver.resolveType(symbolTable, psiType);
                        }
                    }
                }
            }
        }

        // Assignment operations
        if (context instanceof OdinRhsExpressions rhsExpressions) {
            int index = rhsExpressions.getExpressionList().indexOf(topMostExpression);
            PsiElement grandParent = rhsExpressions.getParent();

            if (grandParent instanceof OdinAssignmentStatement statement) {

                List<OdinExpression> lhsExpressions = statement.getLhsExpressions().getExpressionList();
                if (lhsExpressions.size() > index) {
                    OdinExpression lhsExpression = lhsExpressions.get(index);
                    TsOdinType expectedType = lhsExpression.getInferredType(symbolTable);
                    return propagateTypeDown(expectedType, topMostExpression, expression);
                }
            }

            if (grandParent instanceof OdinVariableInitializationStatement odinVariableInitializationStatement) {
                OdinType psiType = odinVariableInitializationStatement.getType();
                if (psiType != null) {
                    TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, psiType);
                    return propagateTypeDown(tsOdinType, topMostExpression, expression);
                }
            }
        }

        // Element entries
        if (context instanceof OdinRhs) {
            OdinCompoundLiteral compoundLiteral = PsiTreeUtil.getParentOfType(context, OdinCompoundLiteralTyped.class, OdinCompoundLiteralUntyped.class);
            Objects.requireNonNull(compoundLiteral);
            OdinElementEntry elemEntry = (OdinElementEntry) context.getParent();

            TsOdinType tsOdinType = null;
            if (compoundLiteral instanceof OdinCompoundLiteralUntyped compoundLiteralUntyped) {
                OdinCompoundLiteralExpression parent = (OdinCompoundLiteralExpression) compoundLiteralUntyped.getParent();
                tsOdinType = parent.getInferredType(symbolTable);
            }

            if (compoundLiteral instanceof OdinCompoundLiteralTyped compoundLiteralTyped) {
                tsOdinType = OdinTypeResolver.resolveType(symbolTable, compoundLiteralTyped.getTypeContainer().getType());
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
                        TsOdinType fieldType = OdinTypeResolver.resolveType(tsOdinType.getSymbolTable(), symbol.getPsiType());
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
                            TsOdinType fieldType = OdinTypeResolver.resolveType(tsOdinType.getSymbolTable(), symbol.getPsiType());
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

        if (context instanceof OdinArgument argument) {
            OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(symbolTable, argument);

            if (!callInfo.callingType().isUnknown()) {
                TsOdinMetaType.MetaType metaType = callInfo.callingType().getMetaType();
                if (metaType == ALIAS) {
                    metaType = callInfo.callingType().baseType(true).getMetaType();
                }

                if (metaType == PROCEDURE) {
                    TsOdinProcedureType callingProcedure = (TsOdinProcedureType) callInfo.callingType().baseType(true);
                    TsOdinProcedureType specializeProcedure = OdinTypeSpecializer.specializeProcedure(symbolTable, callInfo.argumentList(), callingProcedure);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(specializeProcedure.getParameters(), callInfo.argumentList());
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                        return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                    }
                } else if (metaType == PROCEDURE_GROUP) {
                    TsOdinProcedureGroup callingProcedureGroup = (TsOdinProcedureGroup) callInfo.callingType().baseType(true);
                    OdinInferenceEngine.ProcedureRankingResult result = OdinProcedureRanker
                            .findBestProcedure(symbolTable, callingProcedureGroup, callInfo.argumentList());


                    if (result.bestProcedure() != null) {
                        TsOdinProcedureType specializedProcedure = OdinTypeSpecializer.specializeProcedure(
                                symbolTable,
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
                                    symbolTable,
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
                } else if (metaType == STRUCT) {
                    TsOdinStructType structType = (TsOdinStructType) callInfo.callingType().baseType(true);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(structType.getParameters(), callInfo.argumentList());
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                        return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                    }
                } else if (metaType == UNION) {
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

        if (context instanceof OdinCaseClause caseClause) {
            OdinSwitchBlock switchBlock = PsiTreeUtil.getParentOfType(caseClause, OdinSwitchBlock.class);
            if (switchBlock != null) {
                OdinExpression switchBlockExpression = switchBlock.getExpression();
                if (switchBlockExpression == null) {
                    return TsOdinBuiltInTypes.UNKNOWN;
                }
                TsOdinType expectedType = switchBlockExpression.getInferredType(symbolTable);
                return propagateTypeDown(expectedType, topMostExpression, expression);
            }
        }

        if (context instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            OdinType type = constantInitializationStatement.getType();
            if (type != null) {
                return propagateTypeDown(OdinTypeResolver.resolveType(symbolTable, type), topMostExpression, expression);
            }
        }

        if (context instanceof OdinVariableInitializationStatement variableInitializationStatement) {
            OdinType type = variableInitializationStatement.getType();
            if (type != null) {
                return propagateTypeDown(OdinTypeResolver.resolveType(symbolTable, type), topMostExpression, expression);
            }
        }

        if (context instanceof OdinParameterInitialization parameterInitialization) {
            OdinType declaredType = parameterInitialization.getTypeDefinition();
            if (declaredType != null) {
                return propagateTypeDown(OdinTypeResolver.resolveType(symbolTable, declaredType), topMostExpression, expression);
            }
        }
        if (context instanceof OdinIndex index && index.getParent() instanceof OdinIndexExpression indexExpression) {
            TsOdinType tsOdinType = OdinInferenceEngine.inferTypeInExplicitMode(symbolTable, indexExpression.getExpression());
            TsOdinType baseType = tsOdinType.baseType(true);
            if (baseType instanceof TsOdinArrayType tsOdinArrayType) {
                OdinArraySize psiSizeElement = tsOdinArrayType.getPsiSizeElement();
                if (psiSizeElement != null && psiSizeElement.getExpression() != null) {
                    TsOdinType expectedType = inferTypeInExplicitMode(symbolTable, psiSizeElement.getExpression());
                    if (expectedType instanceof TsOdinMetaType tsOdinMetaType) {
                        return propagateTypeDown(tsOdinMetaType.representedType(), topMostExpression, expression);
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
                        OdinVariableInitializationStatement.class,
                        OdinConstantInitializationStatement.class,
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
