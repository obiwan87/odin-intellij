package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record OdinJpsRootTypeResult(
        @NotNull SourceFolder sourceFolder
) implements OdinRootTypeResult {
    @Override
    public @Nullable String collectionName() {
        if (sourceFolder.getJpsElement().getProperties() instanceof OdinCollectionRootProperties collectionRootProperties) {
            return collectionRootProperties.getCollectionName();
        }
        return null;
    }

    @Override
    public VirtualFile directory() {
        return sourceFolder.getFile();
    }

    @Override
    public boolean isSourceRoot() {
        return sourceFolder.getJpsElement().getRootType() == OdinSourceRootType.INSTANCE;
    }

    @Override
    public boolean isCollectionRoot() {
        return sourceFolder.getJpsElement().getRootType() == OdinCollectionRootType.INSTANCE;
    }
}
