package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.*;
import com.lasagnerd.odin.codeInsight.OdinContext;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.TsOdinObjcMemberInfo;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinObjcClass;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinQualifiedType;
import com.lasagnerd.odin.lang.psi.OdinType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class OdinTypeMixin extends OdinPsiElementImpl implements OdinType {
    ParameterizedCachedValue<TsOdinType, OdinTypeResolver.OdinTypeResolverParameters> cachedType;
    CachedValue<List<TsOdinObjcMemberInfo>> cachedObjcClassMembers;

    public OdinTypeMixin(@NotNull ASTNode node) {
        super(node);
    }

    public TsOdinType getResolvedType(OdinTypeResolver.OdinTypeResolverParameters typeResolverParameters) {
        if (!OdinReferenceOwnerMixin.shouldUseCache(typeResolverParameters.context(), this))
            return OdinTypeResolver.resolveType(typeResolverParameters, this);
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
                new OdinContext(), null, null, false
        );

        return getResolvedType(typeResolverParameters);
    }

    public List<TsOdinObjcMemberInfo> getObjcClassMembers() {
        if (this.cachedObjcClassMembers == null) {
            this.cachedObjcClassMembers = createCachedObjcClassMembers();
        }
        return cachedObjcClassMembers.getValue();
    }

    private CachedValue<List<TsOdinObjcMemberInfo>> createCachedObjcClassMembers() {
        return CachedValuesManager.getManager(getProject()).createCachedValue(this::computeObjcClassMembers);
    }

    private CachedValueProvider.Result<List<TsOdinObjcMemberInfo>> computeObjcClassMembers() {
        TsOdinType tsOdinType = getResolvedType();
        if (tsOdinType instanceof TsOdinObjcClass objcClass) {

            List<TsOdinObjcMemberInfo> objcClassMembers = OdinInsightUtils.getObjcClassMembers(objcClass);
            // TODO add dependencies
            Set<Object> dependencies = new HashSet<>();
            dependencies.add(PsiModificationTracker.MODIFICATION_COUNT);
            dependencies.add(this);
            for (TsOdinObjcMemberInfo objcClassMember : objcClassMembers) {
                if (objcClassMember.declaration() != null) {
                    dependencies.add(objcClassMember.declaration());
                }
            }
            return CachedValueProvider.Result.create(objcClassMembers, dependencies);
        }
        return CachedValueProvider.Result.create(Collections.emptyList(), PsiModificationTracker.MODIFICATION_COUNT);
    }

    private CachedValueProvider.@NotNull Result<TsOdinType> resolveType(OdinTypeResolver.OdinTypeResolverParameters typeResolverParameters) {
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(typeResolverParameters, this);
        Set<Object> dependencies = new HashSet<>();
        dependencies.add(this);
        dependencies.add(PsiModificationTracker.MODIFICATION_COUNT);
        if (this instanceof OdinQualifiedType qualifiedType) {

            if (qualifiedType.getTypeIdentifier() != null) {
                dependencies.add(qualifiedType.getTypeIdentifier());
            }

            if (tsOdinType.getDeclaration() != null) {
                dependencies.add(tsOdinType.getDeclaration());
            }
            for (TsOdinType baseType : tsOdinType.baseTypes()) {
                if (baseType.getDeclaration() != null) {
                    dependencies.add(baseType.getDeclaration());
                }
            }
        }

        return CachedValueProvider.Result.create(tsOdinType, dependencies);
    }

    public @NotNull ParameterizedCachedValue<TsOdinType, OdinTypeResolver.OdinTypeResolverParameters> createCachedType() {
        return CachedValuesManager.getManager(getProject()).createParameterizedCachedValue(this::resolveType, false);
    }
}
