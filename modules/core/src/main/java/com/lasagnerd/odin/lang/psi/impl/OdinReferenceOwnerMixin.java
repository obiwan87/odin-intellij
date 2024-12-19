package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.lang.psi.*;
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
        OdinReference odinReference = new OdinReference(context, this);
        odinReference.resolve();
        OdinSymbol symbol = odinReference.getSymbol();

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
        if (!context.isUseCache()) {
            OdinReference odinReference = new OdinReference(context, this);
            odinReference.resolve();
            return odinReference;
        }
        if (getCachedReference() == null) {
            @NotNull ParameterizedCachedValue<OdinReference, OdinContext> cachedValue = CachedValuesManager
                    .getManager(getProject())
                    .createParameterizedCachedValue(this::computeReference, false);

            setCachedReference(cachedValue);
        }
        return getCachedReference().getValue(context);
    }
}
