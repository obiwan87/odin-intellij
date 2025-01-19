package com.lasagnerd.odin.rider;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.codeInsight.imports.OdinCollection;

public interface OdinRiderInteropService {
    static OdinRiderInteropService getInstance(Project project) {
        return project.getService(OdinRiderInteropService.class);
    }

    static boolean isRider(Project project) {
        return getInstance(project) != null;
    }

    void attachSdkRoot(String path);

    void detachSdkRoot(String path);

    OdinCollection getCollection(VirtualFile directoryFile);

    boolean isSourceRoot(VirtualFile directoryFile);
}
