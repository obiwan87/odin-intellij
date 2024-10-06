package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.SourceFolder;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import org.jetbrains.annotations.Nullable;

public record OdinCollectionSearchResult(Module module, ModifiableRootModel modifiableModel, SourceFolder sourceFolder) {
    public @Nullable String collectionName() {
        if(sourceFolder != null) {
            if(sourceFolder.getJpsElement().getProperties() instanceof OdinCollectionRootProperties collectionRootProperties) {
                return collectionRootProperties.getCollectionName();
            }
        }
        return null;
    }
}
