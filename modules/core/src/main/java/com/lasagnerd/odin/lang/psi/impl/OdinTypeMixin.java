package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.lasagnerd.odin.codeInsight.symbols.OdinContext;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinQualifiedType;
import com.lasagnerd.odin.lang.psi.OdinType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public abstract class OdinTypeMixin extends OdinPsiElementImpl implements OdinType {
    ParameterizedCachedValue<TsOdinType, OdinTypeResolver.OdinTypeResolverParameters> cachedType;

    public OdinTypeMixin(@NotNull ASTNode node) {
        super(node);
    }

    public TsOdinType getResolvedType(OdinTypeResolver.OdinTypeResolverParameters typeResolverParameters) {
        if (cachedType == null) {
            cachedType = createCachedType();
        }
        return cachedType.getValue(typeResolverParameters);
    }

    public TsOdinType getResolvedType(OdinContext context) {
        return getResolvedType(new OdinTypeResolver.OdinTypeResolverParameters(context, null, null, false));
    }

    public TsOdinType getResolvedType() {
        OdinTypeResolver.OdinTypeResolverParameters typeResolverParameters = new OdinTypeResolver.OdinTypeResolverParameters(
                null, null, null, false
        );

        return getResolvedType(typeResolverParameters);
    }


    private CachedValueProvider.@NotNull Result<TsOdinType> resolveType(OdinTypeResolver.OdinTypeResolverParameters typeResolverParameters) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(typeResolverParameters, this);
        Set<Object> dependencies = new HashSet<>();
        dependencies.add(this);
        if (this instanceof OdinQualifiedType qualifiedType) {

            dependencies.add(qualifiedType.getTypeIdentifier());
            if (tsOdinType.getDeclaration() != null) {
                dependencies.add(tsOdinType.getDeclaration());
            }
            for (TsOdinType baseType : tsOdinType.baseTypes()) {
                if (baseType.getDeclaration() != null) {
                    dependencies.add(tsOdinType.getDeclaration());
                }
            }
        }

        return CachedValueProvider.Result.create(tsOdinType, dependencies);
    }

    public @NotNull ParameterizedCachedValue<TsOdinType, OdinTypeResolver.OdinTypeResolverParameters> createCachedType() {
        return CachedValuesManager.getManager(getProject()).createParameterizedCachedValue(this::resolveType, false);
    }
}
