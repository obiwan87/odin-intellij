package com.lasagnerd.odin.codeInsight.inlayHints;

import com.intellij.codeInsight.hints.declarative.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinProcedureRanker;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeSpecializer;
import com.lasagnerd.odin.codeInsight.typeSystem.*;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinIdentifier;
import com.lasagnerd.odin.lang.psi.OdinRefExpression;
import com.lasagnerd.odin.lang.psi.OdinUnnamedArgument;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class OdinParameterNameInlayHintsProvider implements InlayHintsProvider {
    @Override
    public @Nullable InlayHintsCollector createCollector(@NonNull PsiFile file, @NonNull Editor editor) {
        return new MyCollector();
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    private static class MyCollector implements SharedBypassCollector {
        @Override
        public void collectFromElement(@NonNull PsiElement element, @NonNull InlayTreeSink sink) {
            if (!(element instanceof OdinUnnamedArgument unnamedArgument)) return;

            OdinContext context = new OdinContext();
            OdinInsightUtils.OdinCallInfo callInfo = OdinInsightUtils.getCallInfo(context, element);
            if (callInfo.callingType().isUnknown()) return;

            TsOdinTypeKind typeKind = callInfo.callingType().getTypeReferenceKind();
            TsOdinType baseType = callInfo.callingType().baseType(true);

            if (typeKind == TsOdinTypeKind.ALIAS && baseType != null) {
                typeKind = baseType.getTypeReferenceKind();
            }

            List<TsOdinParameter> parameters;
            if (typeKind == TsOdinTypeKind.PROCEDURE || typeKind == TsOdinTypeKind.PSEUDO_METHOD || typeKind == TsOdinTypeKind.OBJC_MEMBER) {
                if (!(baseType instanceof TsOdinParameterOwner paramOwner)) return;
                TsOdinParameterOwner specialized = (paramOwner instanceof TsOdinProcedureType procedureType)
                        ? OdinTypeSpecializer.specializeProcedure(context, callInfo.argumentList(), procedureType)
                        : paramOwner;
                parameters = specialized.getParameters();
            } else if (typeKind == TsOdinTypeKind.PROCEDURE_GROUP) {
                if (!(baseType instanceof TsOdinProcedureGroup group)) return;
                OdinInferenceEngine.ProcedureRankingResult result = OdinProcedureRanker.findBestProcedure(context, group, callInfo.argumentList());
                TsOdinProcedureType best = result.bestProcedure();
                if (best == null) return;
                parameters = OdinTypeSpecializer.specializeProcedure(context, callInfo.argumentList(), best).getParameters();
            } else {
                return;
            }

            Map<OdinExpression, TsOdinParameter> argToParam = OdinInsightUtils.getArgumentToParameterMap(parameters, callInfo.argumentList());
            if (argToParam == null) return;

            TsOdinParameter parameter = argToParam.get(unnamedArgument.getExpression());
            if (parameter == null) return;

            String name = parameter.getName();
            if (name == null || name.isEmpty() || name.startsWith("_")) return;

            if (unnamedArgument.getExpression() instanceof OdinRefExpression refExpression) {
                OdinIdentifier identifier = refExpression.getIdentifier();
                if (identifier != null) {
                    if (identifier.getText().equalsIgnoreCase(name))
                        return;
                }
            }

            InlineInlayPosition pos = new InlineInlayPosition(element.getTextRange().getStartOffset(), false, 0);
            sink.addPresentation(pos, null, null, HintFormat.Companion.getDefault(), builder -> {
                builder.text(name + ":", null);
                return null;
            });
        }
    }
}
