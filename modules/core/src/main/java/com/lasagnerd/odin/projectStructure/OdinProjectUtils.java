package com.lasagnerd.odin.projectStructure;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.lasagnerd.odin.projectStructure.collection.OdinCollectionSearchResult;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;

import java.util.Arrays;
import java.util.Objects;

public class OdinProjectUtils {
    public static @Nullable SourceFolder getSourceFolder(@NotNull VirtualFile directoryFile, ModifiableRootModel model) {
        SourceFolder sourceFolder = null;
            sourceFolder = Arrays.stream(model.getContentEntries())
                    .flatMap(c -> c.getSourceFolders(OdinCollectionRootType.INSTANCE).stream())
                    .filter(s -> Objects.equals(s.getFile(), directoryFile))
                    .findFirst()
                    .orElse(null);
        return sourceFolder;
    }

    public static @Nullable OdinCollectionSearchResult findOdinCollection(Project project, VirtualFile file) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);
        if(module != null) {
            ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
            SourceFolder sourceFolder = getSourceFolder(file, modifiableModel);
            if(sourceFolder != null) {
                return new OdinCollectionSearchResult(module, modifiableModel, sourceFolder);
            }
        }
        return null;
    }

    public static OdinCollectionSearchResult findOdinCollection(PsiElement element, String collectionName) {
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if(module != null) {
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

            if(sourceFolder != null) {
                return new OdinCollectionSearchResult(module, modifiableModel, sourceFolder);
            }
        }
        return null;
    }
}
