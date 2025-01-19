package com.lasagnerd.odin.rider;

import com.intellij.openapi.project.Project;

public interface OdinRiderInteropService {
    static OdinRiderInteropService getInstance(Project project) {
        return project.getService(OdinRiderInteropService.class);
    }

    static boolean isRider(Project project) {
        return getInstance(project) != null;
    }

    void attachSdkRoot(String path);

    void detachSdkRoot(String path);
}
