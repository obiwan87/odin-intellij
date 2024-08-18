package com.lasagnerd.odin.sdkConfig;

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

public class OdinSdkLibraryManager {

    public static final String ODIN_SDK_LIBRARY_NAME = "Odin SDK";

    public static void addOrUpdateOdinSdkLibrary(Project project, String sdkPath) {
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library existingLibrary = libraryTable.getLibraryByName(ODIN_SDK_LIBRARY_NAME);

        ApplicationManager.getApplication().runWriteAction(() -> {
            if (existingLibrary != null) {
                // Update the existing library
                Library.ModifiableModel modifiableLibraryModel = existingLibrary.getModifiableModel();
                String[] urls = existingLibrary.getRootProvider().getUrls(OrderRootType.CLASSES);

                for (String url : urls) {
                    modifiableLibraryModel.removeRoot(url, OrderRootType.CLASSES);
                }

                // Add the new SDK path
                VirtualFile sdkVirtualFile = LocalFileSystem.getInstance().findFileByPath(sdkPath);
                if (sdkVirtualFile != null) {
                    modifiableLibraryModel.addRoot(sdkVirtualFile, OrderRootType.CLASSES);
                }

                modifiableLibraryModel.commit();
            } else {
                // Create a new library if it doesn't exist
                LibraryTable.ModifiableModel libraryModel = libraryTable.getModifiableModel();
                Library newLibrary = libraryModel.createLibrary("Odin SDK");
                Library.ModifiableModel modifiableLibraryModel = newLibrary.getModifiableModel();

                // Specify the path to the Odin SDK
                VirtualFile sdkVirtualFile = LocalFileSystem.getInstance().findFileByPath(sdkPath);
                if (sdkVirtualFile != null) {
                    modifiableLibraryModel.addRoot(sdkVirtualFile, OrderRootType.CLASSES);
                }

                modifiableLibraryModel.commit();
                libraryModel.commit();
            }
        });

        // Now associate this library with a module
        addLibraryToModule(project);
    }

    private static void addLibraryToModule(Project project) {
        Module module = ModuleManager.getInstance(project).getModules()[0];
        if (module == null) return;

        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        ModifiableRootModel rootModel = rootManager.getModifiableModel();

        Library library = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraryByName(ODIN_SDK_LIBRARY_NAME);
        if (library != null) {
            LibraryOrderEntry libraryOrderEntry = rootModel.addLibraryEntry(library);
            libraryOrderEntry.setScope(DependencyScope.COMPILE);
        }

        ApplicationManager.getApplication().runWriteAction(rootModel::commit);
    }

    public static boolean isLibrarySet(Project project) {
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library existingLibrary = libraryTable.getLibraryByName(ODIN_SDK_LIBRARY_NAME);
        return existingLibrary != null;
    }
}
