package com.lasagnerd.odin.goToSymbol;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import com.lasagnerd.odin.lang.psi.OdinDeclaration;
import com.lasagnerd.odin.lang.stubs.indexes.OdinAllPublicNamesIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinGoToSymbolContributor implements ChooseByNameContributorEx {
    @Override
    public void processNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
        StubIndex.getInstance().processAllKeys(OdinAllPublicNamesIndex.ALL_PUBLIC_NAMES,
                (Processor<? super String>) (Processor<String>) processor::process,
                scope,
                filter);
    }

    @Override
    public void processElementsWithName(@NotNull String name, @NotNull Processor<? super NavigationItem> processor, @NotNull FindSymbolParameters parameters) {
        StubIndex.getInstance().processElements(OdinAllPublicNamesIndex.ALL_PUBLIC_NAMES,
                name,
                parameters.getProject(),
                parameters.getSearchScope(),
                parameters.getIdFilter(),
                OdinDeclaration.class,
                (Processor<NavigationItem>) processor::process);
    }
}
