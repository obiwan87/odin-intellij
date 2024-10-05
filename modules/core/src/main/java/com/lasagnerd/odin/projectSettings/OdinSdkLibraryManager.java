package com.lasagnerd.odin.projectSettings;

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
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class OdinSdkLibraryManager {

    public static final String ODIN_SDK_LIBRARY_NAME = "Odin SDK";

    public static void addOrUpdateOdinSdkLibrary(Project project, String sdkPath) {
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
        Module module = ModuleManager.getInstance(project).getModules()[0];
        if (module == null) return;

        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        ModifiableRootModel rootModel = rootManager.getModifiableModel();

        if (LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraryByName(ODIN_SDK_LIBRARY_NAME) != null) {
            Library libraryByName = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraryByName(ODIN_SDK_LIBRARY_NAME);
            if(libraryByName != null) {
                LibraryOrderEntry libraryOrderEntry = rootModel.addLibraryEntry(libraryByName);
                libraryOrderEntry.setScope(DependencyScope.COMPILE);
            }
        }

        ApplicationManager.getApplication().runWriteAction(rootModel::commit);
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
