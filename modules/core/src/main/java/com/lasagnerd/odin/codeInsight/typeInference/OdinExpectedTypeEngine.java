package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                    TsOdinType sizeType = OdinInferenceEngine.inferType(symbolTable, psiTypeExpression);
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
                    return OdinInferenceEngine.inferType(symbolTable, lhsExpression);
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
                tsOdinType = inferExpectedType(symbolTable, (OdinExpression) compoundLiteralUntyped.getParent());

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
            @Nullable OdinPsiElement callExpression = PsiTreeUtil.getParentOfType(argument, OdinCallExpression.class, OdinCallType.class);
            if (callExpression != null) {
                TsOdinType tsOdinType;
                List<OdinArgument> argumentList;
                if (callExpression instanceof OdinCallExpression odinCallExpression) {
                    tsOdinType = OdinInferenceEngine.doInferType(symbolTable, odinCallExpression.getExpression());
                    // Here we have to get a meta type, otherwise the call expression does not make sense
                    if (tsOdinType instanceof TsOdinMetaType tsOdinMetaType) {
                        tsOdinType = tsOdinMetaType.representedType();
                    } else if (!(tsOdinType.baseType(true) instanceof TsOdinProcedureType)) {
                        tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
                    }
                    argumentList = odinCallExpression.getArgumentList();
                } else if (callExpression instanceof OdinCallType odinCallType) {
                    tsOdinType = OdinTypeResolver.resolveType(symbolTable, odinCallType.getType());

                    argumentList = odinCallType.getArgumentList();
                } else {
                    tsOdinType = TsOdinBuiltInTypes.UNKNOWN;
                    argumentList = Collections.emptyList();
                }

                TsOdinMetaType.MetaType metaType = tsOdinType.getMetaType();
                if (metaType == ALIAS) {
                    metaType = tsOdinType.baseType(true).getMetaType();
                }

                if (metaType == PROCEDURE) {
                    TsOdinProcedureType callingProcedure = (TsOdinProcedureType) tsOdinType.baseType(true);
                    TsOdinProcedureType specializeProcedure = OdinTypeSpecializer.specializeProcedure(symbolTable, argumentList, callingProcedure);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(specializeProcedure.getParameters(), argumentList);
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                        return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                    }
                } else if (metaType == PROCEDURE_GROUP) {
                    TsOdinProcedureGroup callingProcedureGroup = (TsOdinProcedureGroup) tsOdinType.baseType(true);
                    OdinInferenceEngine.ProcedureRankingResult result = OdinProcedureRanker.findBestProcedure(symbolTable, callingProcedureGroup, argumentList);
                    TsOdinProcedureType callingProcedure = result.bestProcedure();
                    if (callingProcedure != null) {
                        // TODO get specialized parameter type instead
                        Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(callingProcedure.getParameters(), argumentList);
                        if (argumentToParameterMap != null) {
                            TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                            return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                        }
                    } else {
                        boolean allUnnamed = argumentList.stream().allMatch(a -> a instanceof OdinUnnamedArgument);
                        if (!allUnnamed)
                            return TsOdinBuiltInTypes.UNKNOWN;

                        int argumentIndex = argumentList.indexOf(argument);
                        TsOdinType argumentType = null;
                        for (var entry : result.compatibleProcedures()) {
                            TsOdinProcedureType procedureType = entry.getFirst();
                            TsOdinType previousArgument = argumentType;
                            if (procedureType.getParameters().size() > argumentIndex) {
                                argumentType = procedureType.getParameters().get(argumentIndex).getType().baseType();
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
                    // TODO get specialized parameter type instead
                    TsOdinStructType structType = (TsOdinStructType) tsOdinType.baseType(true);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(structType.getParameters(), argumentList);
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                        return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                    }
                } else if (metaType == UNION) {
                    // TODO get specialized parameter type instead
                    TsOdinUnionType unionType = (TsOdinUnionType) tsOdinType.baseType(true);
                    Map<OdinExpression, TsOdinParameter> argumentToParameterMap = OdinInsightUtils.getArgumentToParameterMap(unionType.getParameters(), argumentList);
                    if (argumentToParameterMap != null) {
                        TsOdinParameter tsOdinParameter = argumentToParameterMap.get(topMostExpression);
                        return propagateTypeDown(tsOdinParameter.getType(), topMostExpression, expression);
                    }
                } else if (callExpression instanceof OdinCallExpression odinCallExpression &&
                        (odinCallExpression.getExpression().parenthesesUnwrap() instanceof OdinRefExpression
                                || odinCallExpression.getExpression().parenthesesUnwrap() instanceof OdinTypeDefinitionExpression)) {
                    return propagateTypeDown(tsOdinType, topMostExpression, expression);
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
                return propagateTypeDown(OdinInferenceEngine.inferType(symbolTable, switchBlockExpression), topMostExpression, expression);
            }
        }

        if (context instanceof OdinConstantInitializationStatement constantInitializationStatement) {
            OdinType type = constantInitializationStatement.getType();
            if (type != null) {
                return OdinTypeResolver.resolveType(symbolTable, type);
            }
        }

        if (context instanceof OdinVariableInitializationStatement variableInitializationStatement) {
            OdinType type = variableInitializationStatement.getType();
            if (type != null) {
                return OdinTypeResolver.resolveType(symbolTable, type);
            }
        }

        if (context instanceof OdinParameterInitialization parameterInitialization) {
            OdinType declaredType = parameterInitialization.getTypeDefinition();
            if (declaredType != null) {
                return OdinTypeResolver.resolveType(symbolTable, declaredType);
            }
        }

        if (topMostExpression instanceof OdinBinaryExpression binaryExpression) {
            TsOdinType expectedType = OdinInferenceEngine.inferType(symbolTable, binaryExpression);
            return propagateTypeDown(expectedType, topMostExpression, expression);
        }

        // TODO: add index
        return TsOdinBuiltInTypes.VOID;
    }

    private static TsOdinType propagateTypeDown(TsOdinType expectedType, OdinExpression rhsExpression, OdinExpression position) {
        if (rhsExpression == position)
            return expectedType;


        if (expectedType instanceof TsOdinPointerType pointerType && rhsExpression instanceof OdinAddressExpression addressExpression) {
            return propagateTypeDown(pointerType.getDereferencedType(), addressExpression.getExpression(), position);
        } else if (expectedType instanceof TsOdinArrayType tsOdinArrayType && rhsExpression instanceof OdinIndexExpression indexExpression) {
            return propagateTypeDown(tsOdinArrayType.getElementType(), indexExpression.getExpression(), position);
        } else if (rhsExpression instanceof OdinBinaryExpression binaryExpression) {
            PsiElement prevParent = PsiTreeUtil.findPrevParent(rhsExpression, position);
            if (!(prevParent instanceof OdinExpression nextExpression))
                return TsOdinBuiltInTypes.UNKNOWN;

            if (binaryExpression.getLeft() == nextExpression || binaryExpression.getRight() == nextExpression) {
                return propagateTypeDown(expectedType, nextExpression, position);
            }
        }

        // TODO add more
        return TsOdinBuiltInTypes.UNKNOWN;
    }

    static @Nullable PsiElement findTypeExpectationContext(PsiElement psiElement) {
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
