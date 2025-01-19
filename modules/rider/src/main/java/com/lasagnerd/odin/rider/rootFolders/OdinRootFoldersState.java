package com.lasagnerd.odin.rider.rootFolders;

import com.intellij.util.xmlb.annotations.MapAnnotation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OdinRootFoldersState {
    private final Set<String> sourceRoots = new HashSet<>();

    @MapAnnotation(keyAttributeName = "path", valueAttributeName = "name")
    private final Map<String, String> collectionRoots = new HashMap<>();

    public Set<String> getSourceRoots() {
        return sourceRoots;
    }

    public Map<String, String> getCollectionRoots() {
        return collectionRoots;
    }
}
