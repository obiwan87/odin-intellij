package com.lasagnerd.odin.settings.projectSettings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.riderInterop.OdinRiderInteropService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class OdinSdkLibraryManager {

    public static final String ODIN_SDK_LIBRARY_NAME = "Odin SDK";

    public static void addOrUpdateOdinSdkLibrary(Project project,
                                                 @Nullable String previousPath,
                                                 @NotNull String sdkPath) {
        if (OdinRiderInteropService.isRider(project)) {
            if (previousPath != null && !previousPath.isBlank()) {
                OdinRiderInteropService.getInstance(project).detachSdkRoot(previousPath);
            }
            if (!sdkPath.isBlank()) OdinRiderInteropService.getInstance(project).attachSdkRoot(sdkPath);
        } else {
            if (sdkPath.isBlank()) removeLibrary(project);
            else doAddLibrary(project, sdkPath);
        }
    }

    private static void doAddLibrary(Project project, String sdkPath) {
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library existingLibrary = libraryTable.getLibraryByName(ODIN_SDK_LIBRARY_NAME);

        ApplicationManager.getApplication().runWriteAction(() -> {
            if (existingLibrary != null) {
                // Update the existing collection
                Library.ModifiableModel modifiableLibraryModel = existingLibrary.getModifiableModel();
                String[] urls = existingLibrary.getRootProvider().getUrls(OrderRootType.SOURCES);

                for (String url : urls) {
                    modifiableLibraryModel.removeRoot(url, OrderRootType.SOURCES);
                }

                // Add the new SDK path
                VirtualFile sdkVirtualFile = LocalFileSystem.getInstance().findFileByPath(sdkPath);
                if (sdkVirtualFile != null) {
                    modifiableLibraryModel.addRoot(sdkVirtualFile, OrderRootType.SOURCES);
                }

                modifiableLibraryModel.commit();
            } else {
                // Create a new collection if it doesn't exist
                LibraryTable.ModifiableModel libraryModel = libraryTable.getModifiableModel();
                Library newLibrary = libraryModel.createLibrary("Odin SDK");
                Library.ModifiableModel modifiableLibraryModel = newLibrary.getModifiableModel();

                // Specify the path to the Odin SDK
                VirtualFile sdkVirtualFile = LocalFileSystem.getInstance().findFileByPath(sdkPath);
                if (sdkVirtualFile != null) {
                    modifiableLibraryModel.addRoot(sdkVirtualFile, OrderRootType.SOURCES);
                }

                modifiableLibraryModel.commit();
                libraryModel.commit();
            }
        });

        // Now associate this collection with a module
        addLibraryToModule(project);
    }

    private static void addLibraryToModule(Project project) {
        Library library = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraryByName(ODIN_SDK_LIBRARY_NAME);
        if (library == null) return;
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
            boolean alreadyAttached = java.util.Arrays.stream(rootManager.getOrderEntries())
                    .filter(LibraryOrderEntry.class::isInstance)
                    .map(LibraryOrderEntry.class::cast)
                    .anyMatch(entry -> java.util.Objects.equals(entry.getLibrary(), library));
            if (alreadyAttached) continue;
            ModifiableRootModel rootModel = rootManager.getModifiableModel();
            LibraryOrderEntry entry = rootModel.addLibraryEntry(library);
            entry.setScope(DependencyScope.COMPILE);
            ApplicationManager.getApplication().runWriteAction(rootModel::commit);
        }
    }

    private static void removeLibrary(Project project) {
        LibraryTable table = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library library = table.getLibraryByName(ODIN_SDK_LIBRARY_NAME);
        if (library == null) return;
        ApplicationManager.getApplication().runWriteAction(() -> {
            LibraryTable.ModifiableModel model = table.getModifiableModel();
            model.removeLibrary(library);
            model.commit();
        });
    }

    public static boolean isSdkLibraryConfigured(Project project) {
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library existingLibrary = libraryTable.getLibraryByName(ODIN_SDK_LIBRARY_NAME);
        return existingLibrary != null;
    }

    @Nullable
    public static String findLibraryRootForPath(Project project, String pathToFind) {
        Path path = Path.of(pathToFind);
        for (Library odinLibrary : getOdinLibraries(project)) {
            String[] urls = odinLibrary.getRootProvider().getUrls(OrderRootType.CLASSES);
            for (String url : urls) {
                Path rootPath = Path.of(url.replaceAll("^file://", ""));
                if (path.startsWith(rootPath)) {
                    return rootPath.toAbsolutePath().toString();
                }
            }
        }
        return null;
    }

    public static List<Library> getOdinLibraries(Project project) {
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library existingLibrary = libraryTable.getLibraryByName(ODIN_SDK_LIBRARY_NAME);
        if (existingLibrary != null)
            return List.of(existingLibrary);
        return Collections.emptyList();
    }
}
