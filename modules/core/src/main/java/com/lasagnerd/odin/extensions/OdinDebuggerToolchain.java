package com.lasagnerd.odin.extensions;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;

public interface OdinDebuggerToolchain {
    ExtensionPointName<OdinDebuggerToolchain> DEBUGGER_TOOLCHAIN =
            ExtensionPointName.create("com.lasagnerd.odin.debuggerToolchain");

    String getId();

    String getLabel();

    String[] getExecutableNames();

    boolean isAvailable();

    default boolean isDownloadable() {
        return false;
    }

    String getVersion();

    default String detect() {
        return null;
    }

    default String download(Project project) {
        return null;
    }
}
