package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngine;
import com.lasagnerd.odin.codeInsight.typeInference.OdinInferenceEngineParameters;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinMetaType;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinExpression;
import com.lasagnerd.odin.lang.psi.OdinRefExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class OdinExpressionMixin extends OdinPsiElementImpl implements OdinExpression {
    ParameterizedCachedValue<TsOdinType, OdinInferenceEngineParameters> cachedType;

    public OdinExpressionMixin(@NotNull ASTNode node) {
        super(node);
    }

    public TsOdinType getInferredType(OdinContext context) {
        OdinInferenceEngineParameters parameters = new OdinInferenceEngineParameters(context, null, 1, false);
        return getInferredType(parameters);
    }

    public TsOdinType getInferredType(OdinInferenceEngineParameters parameters) {
        if (!parameters.context().isUseCache()) {
            return OdinInferenceEngine.inferType(parameters, this);
        }

        if (cachedType == null) {
            cachedType = createCachedType();
        }

        return cachedType.getValue(parameters);
    }

    public TsOdinType getInferredType() {
        return getInferredType(OdinInferenceEngineParameters.defaultParameters());
    }

    public @NotNull ParameterizedCachedValue<TsOdinType, OdinInferenceEngineParameters> createCachedType() {
        return CachedValuesManager.getManager(getProject()).createParameterizedCachedValue(this::inferType, false);
    }

    public @NotNull Set<PsiElement> createTypeDependencies(TsOdinType tsOdinType) {
        Set<PsiElement> dependencies = new HashSet<>();
        dependencies.add(OdinExpressionMixin.this);
        if (OdinExpressionMixin.this instanceof OdinRefExpression refExpression) {
            List<OdinRefExpression> refExpressions = OdinInsightUtils.unfoldRefExpressions(refExpression);
            dependencies.addAll(refExpressions);
            if (tsOdinType.getDeclaration() != null) {
                dependencies.add(tsOdinType.getDeclaration());
            }
            for (TsOdinType baseType : tsOdinType.baseTypes()) {
                if (baseType.getDeclaration() != null) {
                    dependencies.add(tsOdinType.getDeclaration());
                }
            }
        }
        return dependencies;
    }

    private CachedValueProvider.Result<TsOdinType> inferType(OdinInferenceEngineParameters inferenceEngineParameters) {
        TsOdinType originalType = OdinInferenceEngine.inferType(inferenceEngineParameters, this);
        TsOdinType tsOdinType;
        if (originalType instanceof TsOdinMetaType metaType) {
            // TODO Performance: should be retrieved from cache?
            tsOdinType = metaType.representedType();
        } else {
            tsOdinType = originalType;
        }

        Set<PsiElement> dependencies = createTypeDependencies(tsOdinType);

        return CachedValueProvider.Result.create(originalType, dependencies);
    }
}
