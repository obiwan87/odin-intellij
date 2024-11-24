package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbolTableResolver;
import com.lasagnerd.odin.codeInsight.typeInference.OdinTypeResolver;
import com.lasagnerd.odin.codeInsight.typeSystem.TsOdinType;
import com.lasagnerd.odin.lang.psi.OdinQualifiedType;
import com.lasagnerd.odin.lang.psi.OdinType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class OdinTypeMixin extends OdinPsiElementImpl implements OdinType {
    ParameterizedCachedValue<TsOdinType, OdinSymbolTable> cachedType;

    public OdinTypeMixin(@NotNull ASTNode node) {
        super(node);
    }

    public TsOdinType getResolvedType(OdinSymbolTable symbolTable) {
        if (cachedType == null) {
            cachedType = createCachedType();
        }
        return cachedType.getValue(symbolTable);
    }

    public TsOdinType getResolvedType() {
        if (cachedType == null) {
            cachedType = createCachedType();
        }
        return cachedType.getValue(null);
    }


    private CachedValueProvider.@NotNull Result<TsOdinType> computeCachedValue(OdinSymbolTable symbolTable) {
        if (symbolTable == null) {
            symbolTable = OdinSymbolTableResolver.computeSymbolTable(this);
        }
        TsOdinType tsOdinType = OdinTypeResolver.resolveType(symbolTable, this);
        List<Object> dependencies = new ArrayList<>();
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

    public @NotNull ParameterizedCachedValue<TsOdinType, OdinSymbolTable> createCachedType() {
        return CachedValuesManager.getManager(getProject()).createParameterizedCachedValue(this::computeCachedValue, false);
    }
}
