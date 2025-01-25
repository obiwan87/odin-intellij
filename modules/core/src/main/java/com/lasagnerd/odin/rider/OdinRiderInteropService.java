package com.lasagnerd.odin.rider;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.codeInsight.imports.OdinCollection;
import com.lasagnerd.odin.projectStructure.collection.OdinRootsService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;

public interface OdinRiderInteropService extends OdinRootsService {
    static OdinRiderInteropService getInstance(Project project) {
        return project.getService(OdinRiderInteropService.class);
    }

    static boolean isRider(Project project) {
        return getInstance(project) != null;
    }

    void attachSdkRoot(String path);

    void detachSdkRoot(String path);

    @Nullable OdinCollection getCollection(@NotNull VirtualFile directoryFile);

    boolean isSourceRoot(VirtualFile directoryFile);

    boolean isUnderRoot(VirtualFile virtualFile);

    Map<String, Path> getCollectionPaths();
}
