package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.dataflow.OdinSymbolValueStore;
import com.lasagnerd.odin.codeInsight.evaluation.OdinBuildFlagEvaluator;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableHelper;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class OdinFileScopeMixin extends OdinPsiElementImpl implements OdinFileScope {

    protected OdinSymbolTable symbolTable;

    private CachedValue<OdinSymbolValueStore> cachedBuildFlagStore;

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
        if (buildFlagsValues.getValues().isEmpty())
            return null;

        List<Object> dependencies = new ArrayList<>(this.getBuildFlagClauseList());
        dependencies.add(this);
        PsiFile containingFile = this.getContainingFile();
        if (containingFile != null) {
            dependencies.add(containingFile);
        }
        return CachedValueProvider.Result.create(buildFlagsValues, dependencies);
    }

    public OdinFileScopeMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        symbolTable = null;
    }

    public OdinSymbolTable getFullSymbolTable() {
        if (symbolTable == null) {
            symbolTable = OdinSymbolTableHelper
                    .buildFileScopeSymbolTable(this,
                            OdinSymbolTableHelper.getGlobalFileVisibility(this));
        }

        return symbolTable;
    }
}
