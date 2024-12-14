package com.lasagnerd.odin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.lasagnerd.odin.codeInsight.OdinSymbolTable;
import com.lasagnerd.odin.codeInsight.dataflow.OdinSymbolValueStore;
import com.lasagnerd.odin.codeInsight.evaluation.EvOdinValueSet;
import com.lasagnerd.odin.codeInsight.evaluation.OdinBuildFlagEvaluator;
import com.lasagnerd.odin.codeInsight.symbols.OdinSymbol;
import com.lasagnerd.odin.codeInsight.symbols.symbolTable.OdinSymbolTableHelper;
import com.lasagnerd.odin.lang.psi.OdinFileScope;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
        if (this.getBuildFlagClauseList().isEmpty())
            return CachedValueProvider.Result.create(null, this);

        OdinBuildFlagEvaluator buildFlagEvaluator = new OdinBuildFlagEvaluator();
        Map<OdinSymbol, EvOdinValueSet> buildFlagsValues = buildFlagEvaluator.evaluate(this.getBuildFlagClauseList());
        if (buildFlagsValues.isEmpty())
            return CachedValueProvider.Result.create(null, this.getBuildFlagClauseList());
        OdinSymbolValueStore odinSymbolValueStore = new OdinSymbolValueStore();
        odinSymbolValueStore.getValues().putAll(buildFlagsValues);
        return CachedValueProvider.Result.create(odinSymbolValueStore, this.getBuildFlagClauseList());
    }

    public OdinFileScopeMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        symbolTable = null;
    }

    public OdinSymbolTable getFullSymbolTable() {
        if (this instanceof OdinFileScope odinFileScope) {
            if (symbolTable == null) {
                symbolTable = OdinSymbolTableHelper.buildFileScopeSymbolTable(
                        odinFileScope,
                        OdinSymbolTableHelper.getGlobalFileVisibility(odinFileScope)
                );
            }

            return symbolTable;
        }
        return null;
    }
}
