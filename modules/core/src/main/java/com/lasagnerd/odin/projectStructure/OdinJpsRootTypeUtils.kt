package com.lasagnerd.odin.projectStructure;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.projectStructure.collection.OdinRootTypeResult;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OdinRootTypeUtils {
    public static @Nullable SourceFolder getCollectionFolder(@NotNull VirtualFile directoryFile, ModifiableRootModel model) {
        return getRootTypeSourceFolder(directoryFile, model, OdinCollectionRootType.INSTANCE);
    }

    public static @Nullable SourceFolder getSourceFolder(@NotNull VirtualFile directoryFile, ModifiableRootModel model) {
        return getRootTypeSourceFolder(directoryFile, model, OdinSourceRootType.INSTANCE);
    }

    private static @Nullable SourceFolder getRootTypeSourceFolder(@NotNull VirtualFile directoryFile,
                                                                  ModifiableRootModel model,
                                                                  JpsModuleSourceRootType<?>... sourceRootType) {
        Set<JpsModuleSourceRootType<?>> sourceRootTypes = Arrays.stream(sourceRootType).collect(Collectors.toSet());
        return Arrays.stream(model.getContentEntries())
                .flatMap(c -> c.getSourceFolders(sourceRootTypes).stream())
                .filter(s -> Objects.equals(s.getFile(), directoryFile))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if the passed virtual file is a collection source root.
     *
     * @param project The project
     * @param file    The virtual file to check
     * @return A search results
     */
    public static @Nullable OdinRootTypeResult findCollectionRoot(Project project, VirtualFile file) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);
        if (module != null) {
            ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
            SourceFolder sourceFolder = getCollectionFolder(file, modifiableModel);
            if (sourceFolder != null) {
                return new OdinRootTypeResult(module, modifiableModel, sourceFolder);
            }
        }
        return null;
    }

    public static OdinRootTypeResult findCollectionRoot(PsiElement element, String collectionName) {
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if (module != null) {
            ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
            SourceFolder sourceFolder = Arrays.stream(modifiableModel.getContentEntries()).flatMap(
                    c -> c.getSourceFolders(OdinCollectionRootType.INSTANCE).stream()
            ).filter(s -> {

                JpsElement properties = s.getJpsElement().getProperties();
                if (properties instanceof OdinCollectionRootProperties collectionRootProperties) {
                    return Objects.equals(collectionRootProperties.getCollectionName(), collectionName);
                }
                return false;
            }).findFirst().orElse(null);

            if (sourceFolder != null) {
                return new OdinRootTypeResult(module, modifiableModel, sourceFolder);
            }
        }
        return null;
    }

    public static @Nullable OdinRootTypeResult findContainingCollection(Project project, @NotNull VirtualFile virtualFile) {
        VirtualFile sourceRootForFile = ProjectFileIndex.getInstance(project)
                .getSourceRootForFile(virtualFile);

        return findCollectionRoot(project, sourceRootForFile);
    }

    public static @Nullable OdinRootTypeResult findContainingRoot(Project project, @NotNull VirtualFile virtualFile) {
        VirtualFile file = ProjectFileIndex.getInstance(project)
                .getSourceRootForFile(virtualFile);

        if (file == null)
            return null;

        Module module = ModuleUtilCore.findModuleForFile(file, project);
        if (module != null) {
            ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
            SourceFolder sourceFolder = getRootTypeSourceFolder(file, modifiableModel, OdinSourceRootType.INSTANCE, OdinCollectionRootType.INSTANCE);
            if (sourceFolder != null) {
                return new OdinRootTypeResult(module, modifiableModel, sourceFolder);
            }
        }
        return null;
    }
}
