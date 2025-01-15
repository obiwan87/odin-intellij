package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.lasagnerd.odin.codeInsight.OdinInsightUtils;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.dataflow.OdinSymbolValueStore;
import com.lasagnerd.odin.codeInsight.evaluation.OdinBuildFlagEvaluator;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableHelper;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import com.lasagnerd.odin.lang.stubs.OdinFileScopeStub;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class OdinFileScopeMixin extends OdinStubbedElementImpl<OdinFileScopeStub> implements OdinFileScope {

    protected OdinSymbolTable symbolTable;

    private CachedValue<OdinSymbolValueStore> cachedBuildFlagStore;

    private CachedValue<OdinSymbolTable> cachedSymbolTable;

    public OdinFileScopeMixin(OdinFileScopeStub stub, IElementType nodeType, ASTNode node) {
        super(stub, nodeType, node);
    }

    public OdinFileScopeMixin(@NotNull OdinFileScopeStub stub, @NotNull IStubElementType<?, ?> nodeType) {
        super(stub, nodeType);
    }

    public OdinSymbolValueStore getBuildFlagsValuesStore() {
        if (cachedBuildFlagStore == null) {
            cachedBuildFlagStore = CachedValuesManager.getManager(getProject()).createCachedValue(
                    this::computeBuildFlagStore
            );
        }
        return cachedBuildFlagStore.getValue();
    }

    private CachedValueProvider.Result<OdinSymbolValueStore> computeBuildFlagStore() {
        OdinBuildFlagEvaluator buildFlagEvaluator = new OdinBuildFlagEvaluator();
        OdinSymbolValueStore buildFlagsValues = buildFlagEvaluator.evaluateBuildFlags(this.getContainingOdinFile());
        if (buildFlagsValues.isEmpty())
            return null;

        List<Object> dependencies = new ArrayList<>(this.getBuildFlagClauseList());
        dependencies.add(this);
        PsiFile containingFile = this.getContainingFile();
        dependencies.add(containingFile);
        return CachedValueProvider.Result.create(buildFlagsValues, dependencies);
    }

    public OdinFileScopeMixin(@NotNull ASTNode node) {
        super(node);
    }

    public OdinSymbolTable getSymbolTable() {
        if (cachedSymbolTable == null) {
            cachedSymbolTable = CachedValuesManager.getManager(getProject())
                    .createCachedValue(this::computeSymbolTable);
        }

        return cachedSymbolTable.getValue();
    }

    private CachedValueProvider.Result<OdinSymbolTable> computeSymbolTable() {
        OdinSymbolTable symbolTable = OdinSymbolTableHelper
                .buildFileScopeSymbolTable(this,
                        OdinInsightUtils.getGlobalFileVisibility(this));

        return CachedValueProvider.Result.create(
                symbolTable, this, PsiModificationTracker.MODIFICATION_COUNT
        );
    }
}
