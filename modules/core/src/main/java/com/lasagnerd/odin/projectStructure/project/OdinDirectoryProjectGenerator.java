package com.lasagnerd.odin.projectStructure.project;

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileUtil;
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.DirectoryProjectGeneratorBase;
import com.intellij.platform.ProjectGeneratorPeer;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.projectStructure.module.OdinModuleType;
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType;
import com.lasagnerd.odin.projectSettings.OdinProjectConfigurable;
import com.lasagnerd.odin.projectSettings.OdinProjectSettings;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsService;
import com.lasagnerd.odin.projectSettings.OdinProjectSettingsState;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

public class OdinDirectoryProjectGenerator extends DirectoryProjectGeneratorBase<OdinProjectSettings> implements CustomStepProjectGenerator<OdinProjectSettings> {
    @Override
    public @NotNull @NlsContexts.Label String getName() {
        return "Odin";
    }

    @Override
    public @Nullable Icon getLogo() {
        return OdinIcons.OdinFileType;
    }

    @Override
    public void generateProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull OdinProjectSettings settings, @NotNull Module module) {
        OdinProjectSettingsState state = new OdinProjectSettingsState();
        OdinProjectConfigurable.apply(state, settings);


        OdinProjectSettingsService.getInstance(project).loadState(state);
        try {
            WriteAction.run(() -> {
                VirtualFile srcDir = baseDir.createChildDirectory(this, "src");
                VirtualFile mainOdin = srcDir.createChildData(this, "main.odin");
                @Language("Odin")
                String mainOdinContent = """
                        package main
                        
                        import "core:fmt"
                        
                        main :: proc() {
                            fmt.println("Hi Mom!");
                        }
                        """;
                VirtualFileUtil.writeText(mainOdin, mainOdinContent);
                String basePath = project.getBasePath();

                ModuleManager moduleManager = ModuleManager.getInstance(project);
                // remove existing modules
                {
                    ModifiableModuleModel modifiableModuleModel = moduleManager.getModifiableModel();
                    Module[] existingModules = moduleManager.getModules();
                    for (Module existingModule : existingModules) {
                        modifiableModuleModel.disposeModule(existingModule); // Remove the existing default module
                    }
                    modifiableModuleModel.commit();
                }

                if (basePath != null) {
                    Path imlFilePath = Path.of(basePath, project.getName() + ".iml");
                    Module odinModule = moduleManager.newModule(imlFilePath, OdinModuleType.ODIN_MODULE);
                    ModifiableRootModel modifiableModuleModel = ModuleRootManager.getInstance(odinModule).getModifiableModel();
                    ContentEntry contentEntry = modifiableModuleModel.addContentEntry(baseDir);
                    contentEntry.addSourceFolder(srcDir, OdinSourceRootType.INSTANCE);
                    modifiableModuleModel.commit();
                }

            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configureModule(@NotNull Module module, @NotNull VirtualFile baseDir, @NotNull OdinProjectSettings settings) {

        super.configureModule(module, baseDir, settings);
    }

    @Override
    public @Nullable @Nls(capitalization = Nls.Capitalization.Sentence) String getDescription() {
        return "Creates a new Odin project";
    }

    @Override
    public @NotNull ProjectGeneratorPeer<OdinProjectSettings> createPeer() {
        return new OdinProjectGeneratorPeer();
    }


    @Override
    public AbstractActionWithPanel createStep(DirectoryProjectGenerator<OdinProjectSettings> projectGenerator, AbstractNewProjectStep.AbstractCallback<OdinProjectSettings> callback) {
        return new OdinProjectSettingsStep(projectGenerator, callback);
    }
}
