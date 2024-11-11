package com.lasagnerd.odin.projectStructure.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.lasagnerd.odin.projectSettings.OdinSdkUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public class OdinLibraryProvider extends AdditionalLibraryRootsProvider {
    @Override
    public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
        List<SyntheticLibrary> libraries = new ArrayList<>();

        Optional<String> sdkPathOptional = OdinSdkUtils.getSdkPath(project);
        if (sdkPathOptional.isPresent()) {
            String odinBinaryPath = OdinSdkUtils.getOdinBinaryPath(project);
            if (odinBinaryPath != null) {
                // Path is valid
                VirtualFile baseDir = VirtualFileManager.getInstance().findFileByNioPath(Path.of(sdkPathOptional.get()));
                if(baseDir != null) {
                    OdinSdkLibrary odinSdkLibrary = new OdinSdkLibrary(baseDir);
                    libraries.add(odinSdkLibrary);
                }
            }
        }

        return libraries;
    }
}
