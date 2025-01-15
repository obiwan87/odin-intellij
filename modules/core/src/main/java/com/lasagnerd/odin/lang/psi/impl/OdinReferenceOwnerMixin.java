package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.dataflow.OdinLattice;
import com.lasagnerd.odin.codeInsight.symbols.OdinReferenceResolver;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.lang.psi.*;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public abstract class OdinReferenceOwnerMixin extends OdinPsiElementImpl implements OdinReferenceOwner, OdinIdentifier {
    private ParameterizedCachedValue<OdinReference, OdinContext> cachedReference;

    public OdinReferenceOwnerMixin(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public OdinReference getReference() {
        return getReference(new OdinContext());
    }

    CachedValueProvider.Result<OdinReference> computeReference(OdinContext context) {
        OdinSymbol symbol = OdinReferenceResolver.resolve(context, this);
        OdinReference odinReference = new OdinReference(this, symbol);

        List<Object> dependencies = new ArrayList<>();
        dependencies.add(this);
        if (symbol != null) {
            if (symbol.getDeclaration() != null) {
                dependencies.add(symbol.getDeclaration());
            }
            if (symbol.getPsiType() != null) {
                dependencies.add(symbol.getPsiType());
            }
        }

        if (this.getParent() instanceof OdinRefExpression) {
            List<OdinRefExpression> refExpressions = OdinInsightUtils.unfoldRefExpressions(this);
            dependencies.addAll(refExpressions);
        }

        if (this.getParent().getParent() instanceof OdinQualifiedType qualifiedType) {
            dependencies.add(qualifiedType.getIdentifier());
        }

        return CachedValueProvider.Result.create(odinReference, dependencies);
    }

    public @NotNull OdinReference getReference(OdinContext context) {
        if (OdinProjectSettingsService.getInstance(getProject()).isConditionalSymbolResolutionEnabled()) {
            return doGetReferenceWithKnowledge(context);
        }
        return doGetReferenceWithoutKnowledge(context);
    }

    private @NotNull OdinReference doGetReferenceWithoutKnowledge(OdinContext context) {
        if (!shouldUseCache(context, this)) {
            OdinSymbol symbol = OdinReferenceResolver.resolve(context, this);
            return new OdinReference(this, symbol);
        }

        if (getCachedReference() == null) {
            @NotNull ParameterizedCachedValue<OdinReference, OdinContext> cachedValue = CachedValuesManager
                    .getManager(getProject())
                    .createParameterizedCachedValue(this::computeReference, false);

            setCachedReference(cachedValue);
        }
        return getCachedReference().getValue(context);
    }

    private OdinReference doGetReferenceWithKnowledge(OdinContext context) {
        boolean useCache = shouldUseCache(context, this);
        if (!useCache) {
            OdinSymbol symbol = OdinReferenceResolver.resolve(context, this);
            return new OdinReference(this, symbol);
        }

        if (getCachedReference() == null) {
            @NotNull ParameterizedCachedValue<OdinReference, OdinContext> cachedValue = CachedValuesManager
                    .getManager(getProject())
                    .createParameterizedCachedValue(this::computeReference, false);

            setCachedReference(cachedValue);
        }
        return getCachedReference().getValue(context);
    }

    public static boolean shouldUseCache(OdinContext context, PsiElement element) {
        Project project = element.getProject();
        if (!OdinProjectSettingsService.getInstance(project).isCacheEnabled())
            return false;

        if (context.isUseKnowledge()) {
            OdinLattice explicitKnowledge = OdinReferenceResolver.computeExplicitKnowledge(context, element);
            OdinLattice implicitKnowledge = OdinReferenceResolver.computeImplicitKnowledge(element);
            return explicitKnowledge.getSymbolValueStore().isEmpty() || explicitKnowledge.isSubset(implicitKnowledge);
        }
        return true;
    }
}
