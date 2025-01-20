package com.lasagnerd.odin.rider.rootFolders;

import com.intellij.util.xmlb.annotations.MapAnnotation;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class OdinRootFoldersState {
    public final Set<String> sourceRoots = new HashSet<>();

    @MapAnnotation(keyAttributeName = "path", valueAttributeName = "name")
    public final Map<String, String> collectionRoots = new HashMap<>();
}
