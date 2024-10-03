package com.lasagnerd.odin.module;

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileUtil;
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.DirectoryProjectGeneratorBase;
import com.intellij.platform.ProjectGeneratorPeer;
import com.lasagnerd.odin.OdinIcons;
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
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
