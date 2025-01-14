package com.lasagnerd.odin.projectStructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.ols.OlsCollection;
import com.lasagnerd.odin.ols.OlsConfiguration;
import com.lasagnerd.odin.ols.OlsProfile;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsState;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class OdinProjectMigration {

    public static void checkProject(Project project) {
        ApplicationManager.getApplication().runReadAction(() -> {
            Module[] modules = ModuleManager.getInstance(project).getModules();
            if (modules.length != 1)
                return;
            List<Module> modulesToMigrate = new ArrayList<>();
            for (Module module : modules) {
                @NotNull Collection<VirtualFile> odinFiles = FileTypeIndex.getFiles(OdinFileType.INSTANCE, GlobalSearchScope.allScope(project));
                if (!odinFiles.isEmpty()) {
                    ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
                    List<VirtualFile> sourceRoots = modifiableModel.getSourceRoots(OdinSourceRootType.INSTANCE);
                    if (sourceRoots.isEmpty()) {
                        modulesToMigrate.add(module);
                    }
                }
            }

            if (modulesToMigrate.isEmpty())
                return;

            Notification migrationNotification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("Odin Notifications")
                    .createNotification("You seem to be using Odin, but no source directories were found.", NotificationType.INFORMATION)
                    .addAction(new OdinModuleMigrationAction(modulesToMigrate));

            NotificationsManager
                    .getNotificationsManager()
                    .showNotification(migrationNotification, project);
        });
    }

    public static void migrateModule(Module module) {
        Project project = module.getProject();
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        String basePath = project.getBasePath();
        if (basePath == null)
            return;

        VirtualFile baseDir = virtualFileManager.findFileByNioPath(Path.of(basePath));
        if (baseDir == null)
            return;

        WriteAction.run(() -> {
            VirtualFile olsFile = virtualFileManager.findFileByNioPath(Path.of(basePath, "ols.json"));

            // Create a new Odin module, if there's an ols file, set the checker paths
            // as source roots and the collection paths as collection roots
            // if not, look for a src-Folder under the base path

            ModifiableRootModel modifiableRootModel = ModuleRootManager.getInstance(module)
                    .getModifiableModel();
            ContentEntry contentEntry = modifiableRootModel.addContentEntry(baseDir);

            OdinProjectSettingsState state = OdinProjectSettingsService.getInstance(project).getState();

            if (olsFile != null) {
                importOlsConfiguration(state, contentEntry, olsFile);
            } else {
                Path srcPath = Path.of(basePath, "src");
                VirtualFile srcPathDir = virtualFileManager.findFileByNioPath(srcPath);
                if (srcPathDir != null) {
                    contentEntry.addSourceFolder(
                            srcPathDir,
                            OdinSourceRootType.INSTANCE
                    );
                }
            }
            OdinProjectSettingsService.getInstance(project).loadState(state);
            modifiableRootModel.commit();
        });
    }

    public static void importOlsConfiguration(OdinProjectSettingsState state, ContentEntry contentEntry, VirtualFile olsFile) {
        Document document = FileDocumentManager.getInstance().getDocument(olsFile);
        if (document != null) {
            String olsFileContent = document.getText();
            ObjectMapper objectMapper = new ObjectMapper();
            OlsConfiguration olsConfiguration;
            try {
                olsConfiguration = objectMapper.readValue(olsFileContent, OlsConfiguration.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            VirtualFileManager virtualFileManager1 = VirtualFileManager.getInstance();
            String olsDir = olsFile.getParent().getPath();
            for (OlsCollection collection : olsConfiguration.getCollections()) {
                String collectionName = collection.getName();
                String collectionPath = collection.getPath();
                Path collectionNioPath = Path.of(olsDir, collectionPath);
                VirtualFile collectionDir = virtualFileManager1.findFileByNioPath(collectionNioPath);
                if (collectionDir != null) {
                    contentEntry.addSourceFolder(
                            collectionDir,
                            OdinCollectionRootType.INSTANCE,
                            new OdinCollectionRootProperties(collectionName)
                    );
                }
            }

            String profile = olsConfiguration.getProfile();
            if (profile != null) {
                List<OlsProfile> profiles = olsConfiguration.getProfiles();
                if (profiles != null) {
                    OlsProfile olsProfile = profiles.stream()
                            .filter(p -> Objects.equals(p.getName(), profile))
                            .findFirst()
                            .orElse(null);
                    if (olsProfile != null) {
                        List<String> checkerPaths = olsProfile.getCheckerPath();
                        if (checkerPaths != null) {
                            for (String checkerPath : checkerPaths) {
                                VirtualFile checkerPathDir = virtualFileManager1.findFileByNioPath(Path.of(olsDir, checkerPath));
                                if (checkerPathDir != null) {
                                    contentEntry.addSourceFolder(
                                            checkerPathDir,
                                            OdinSourceRootType.INSTANCE
                                    );
                                }
                            }
                        }
                    }
                }
            }

            String checkerArgs = olsConfiguration.getCheckerArgs();
            if(checkerArgs != null) {
                state.setExtraBuildFlags(checkerArgs);
            }
        }
    }
}
