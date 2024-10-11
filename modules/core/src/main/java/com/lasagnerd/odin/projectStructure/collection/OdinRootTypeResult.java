package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record OdinRootTypeResult(
        Module module,
        ModifiableRootModel modifiableModel,
        @NotNull SourceFolder sourceFolder
) {
    public @Nullable String collectionName() {
        if (sourceFolder.getJpsElement().getProperties() instanceof OdinCollectionRootProperties collectionRootProperties) {
            return collectionRootProperties.getCollectionName();
        }
        return null;
    }

    public VirtualFile directory() {
        return sourceFolder.getFile();
    }

    public boolean isSourceRoot() {
        return sourceFolder.getJpsElement().getRootType() == OdinSourceRootType.INSTANCE;
    }

    public boolean isCollectionRoot() {
        return sourceFolder.getJpsElement().getRootType() == OdinCollectionRootType.INSTANCE;
    }
}
