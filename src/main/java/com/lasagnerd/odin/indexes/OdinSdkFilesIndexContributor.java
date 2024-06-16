package com.lasagnerd.odin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.IndexableSetContributor;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.sdkConfig.OdinSdkConfigPersistentState;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class OdinSdkFilesIndexContributor extends IndexableSetContributor {

    @Override
    public @NotNull Set<VirtualFile> getAdditionalRootsToIndex() {

        return Set.of();
    }

    @NotNull
    public Set<VirtualFile> getAdditionalProjectRootsToIndex(@NotNull Project project) {
        Optional<String> sdkPath = OdinSdkConfigPersistentState.getSdkPath(project);
        if (sdkPath.isPresent()) {
            try {
                Path path = Path.of(sdkPath.get());
                VirtualFile sdkRoot = VfsUtil.findFile(path, false);
                if (sdkRoot != null) {
                    return VfsUtil.collectChildrenRecursively(sdkRoot)
                            .stream()
                            .filter(vf -> vf.getFileType() == OdinFileType.INSTANCE)
                            .collect(Collectors.toSet());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return Collections.emptySet();
    }
}