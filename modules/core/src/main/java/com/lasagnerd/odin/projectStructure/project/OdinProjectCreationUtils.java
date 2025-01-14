package com.lasagnerd.odin.projectStructure.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileUtil;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectConfigurable;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettings;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettingsState;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OdinProjectCreationUtils {
    public static void saveProjectSettings(@NotNull Project project, @NotNull OdinProjectSettings settings) {
        OdinProjectSettingsState state = new OdinProjectSettingsState();
        OdinProjectConfigurable.apply(state, settings);
        OdinProjectSettingsService.getInstance(project).loadState(state);
    }

    public static @NotNull VirtualFile createSampleCode(Object requestor, @NotNull VirtualFile baseDir) throws IOException {
        VirtualFile srcDir = baseDir.createChildDirectory(requestor, "src");
        VirtualFile mainOdin = srcDir.createChildData(requestor, "main.odin");
        @Language("Odin")
        String mainOdinContent = """
                package main
                
                import "core:fmt"
                
                main :: proc() {
                    fmt.println("Hi, mom!");
                }
                """;
        VirtualFileUtil.writeText(mainOdin, mainOdinContent);
        return srcDir;
    }

    public static void setupNewModule(@NotNull Project project,
                                      @NotNull VirtualFile baseDir,
                                      VirtualFile srcDir) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module odinModule = moduleManager.findModuleByName(project.getName());
        if (odinModule == null)
            return;
        setupModule(baseDir, srcDir, odinModule);
    }

    public static void setupModule(@NotNull VirtualFile baseDir, VirtualFile srcDir, Module odinModule) {
        ModifiableRootModel modifiableModuleModel = ModuleRootManager.getInstance(odinModule).getModifiableModel();
        ContentEntry contentEntry = modifiableModuleModel.addContentEntry(baseDir);
        contentEntry.addSourceFolder(srcDir, OdinSourceRootType.INSTANCE);
        modifiableModuleModel.commit();
    }
}
