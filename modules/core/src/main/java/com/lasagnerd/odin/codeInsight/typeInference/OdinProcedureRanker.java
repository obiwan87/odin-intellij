package com.lasagnerd.odin.codeInsight.typeInference;

import com.intellij.openapi.util.Pair;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OdinProcedureRanker {
    public static @NotNull OdinInferenceEngine.ProcedureRankingResult findBestProcedure(OdinContext context, TsOdinProcedureGroup procedureGroup, @NotNull List<OdinArgument> argumentList) {
        List<Pair<TsOdinProcedureType, List<Pair<TsOdinType, OdinTypeChecker.TypeCheckResult>>>> compatibleProcedures = new ArrayList<>();
        for (TsOdinProcedureType targetProcedure : procedureGroup.getProcedures()) {
            @Nullable Map<OdinExpression, TsOdinParameter> argumentExpressions = OdinInsightUtils.getArgumentToParameterMap(targetProcedure.getParameters(), argumentList);
            if (argumentExpressions == null) continue;

            boolean allParametersCompatible = true;

            // Gather all compatible procedures along with their original argument
            List<Pair<TsOdinType, OdinTypeChecker.TypeCheckResult>> compatibilityResults = new ArrayList<>();
            for (var entry : argumentExpressions.entrySet()) {
                TsOdinParameter tsOdinParameter = entry.getValue();
                OdinExpression argumentExpression = entry.getKey();

                TsOdinType parameterType = tsOdinParameter.getType().baseType();
                TsOdinType argumentType = inferArgumentType(context, argumentExpression, parameterType);

                OdinTypeChecker.TypeCheckResult compatibilityResult = OdinTypeChecker.checkTypes(argumentType,
                        parameterType,
                        tsOdinParameter.isAnyInt());

                if (!compatibilityResult.isCompatible()) {
                    allParametersCompatible = false;
                    break;
                }
                compatibilityResults.add(Pair.create(argumentType, compatibilityResult));
            }
            if (allParametersCompatible) {
                compatibleProcedures.add(Pair.create(targetProcedure, compatibilityResults));
            }
        }

        TsOdinProcedureType bestProcedure = null;
        if (compatibleProcedures.size() == 1) {
            bestProcedure = compatibleProcedures.getFirst().getFirst();

        } else if (!compatibleProcedures.isEmpty()) {
            bestProcedure = breakTie(compatibleProcedures);
        }
        return new OdinInferenceEngine.ProcedureRankingResult(compatibleProcedures, bestProcedure);
    }

    public static TsOdinType inferArgumentType(OdinContext context,
                                               OdinExpression argumentExpression,
                                               TsOdinType expectedType) {
        TsOdinType argumentType = TsOdinBuiltInTypes.UNKNOWN;
        TsOdinType expectedBaseType = expectedType.baseType(true);

        if (argumentExpression instanceof OdinImplicitSelectorExpression implicitSelectorExpression) {
            if (expectedBaseType instanceof TsOdinEnumType enumType) {
                if (OdinInferenceEngine.enumContainsValue(enumType, implicitSelectorExpression.getIdentifier().getText())) {
                    argumentType = expectedType;
                }
            }
        } else if (argumentExpression instanceof OdinCompoundLiteralExpression compoundLiteralExpression
                && compoundLiteralExpression.getCompoundLiteral() instanceof OdinCompoundLiteralUntyped) {
            if (expectedBaseType instanceof TsOdinBitSetType
                    || expectedBaseType instanceof TsOdinStructType
                    || expectedBaseType instanceof TsOdinArrayType
                    || expectedBaseType instanceof TsOdinSliceType
                    || expectedBaseType instanceof TsOdinMatrixType) {
                argumentType = expectedType;
            }
        } else {
            argumentType = argumentExpression.getInferredType().baseType();
        }
        return argumentType;
    }

    private static @Nullable TsOdinProcedureType breakTie(List<Pair<TsOdinProcedureType, List<Pair<TsOdinType, OdinTypeChecker.TypeCheckResult>>>> compatibleProcedures) {
        // tie-breaker if possible

        List<Pair<TsOdinProcedureType, Integer>> scores = new ArrayList<>();
        for (var compatibleProcedure : compatibleProcedures) {
            int conversionCost = 0;
            TsOdinProcedureType tsOdinProcedureType = compatibleProcedure.getFirst();
            for (var compatibilityResult : compatibleProcedure.getSecond()) {

                var result = compatibilityResult.getSecond();
                conversionCost += result.getConversionActionList().size();
            }
            scores.add(Pair.create(tsOdinProcedureType, conversionCost));
        }

        Integer minConversionCost = scores.stream().mapToInt(p -> p.getSecond()).min().orElseThrow();
        List<Pair<TsOdinProcedureType, Integer>> minConversionCosts = scores.stream().filter(p -> p.getSecond().equals(minConversionCost)).toList();
        TsOdinProcedureType bestProcedure;
        if (minConversionCosts.size() == 1) {
            bestProcedure = minConversionCosts.getFirst().getFirst();
        } else {
            bestProcedure = null;
        }
        return bestProcedure;
    }
}
