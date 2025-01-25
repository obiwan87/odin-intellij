package com.lasagnerd.odin.projectStructure.collection;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public interface OdinRootTypeResult {
    @Nullable String collectionName();

    VirtualFile directory();

    boolean isSourceRoot();

    boolean isCollectionRoot();
}
