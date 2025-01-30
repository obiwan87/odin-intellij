package com.lasagnerd.odin.projectStructure.project;

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.DirectoryProjectGeneratorBase;
import com.intellij.platform.ProjectGeneratorPeer;
import com.lasagnerd.odin.OdinIcons;
import com.lasagnerd.odin.settings.projectSettings.OdinProjectSettings;
import lombok.Getter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

@Getter
public class OdinDirectoryProjectGenerator
        extends DirectoryProjectGeneratorBase<OdinProjectSettings>
        implements CustomStepProjectGenerator<OdinProjectSettings> {

    private OdinProjectGeneratorPeer peer;

    @Override
    public @NotNull @NlsContexts.Label String getName() {
        return "Odin";
    }

    @Override
    public @Nullable Icon getLogo() {
        return OdinIcons.OdinFileType;
    }

    @Override
    public void generateProject(@NotNull Project project,
                                @NotNull VirtualFile baseDir,
                                @NotNull OdinProjectSettings settings,
                                @NotNull Module module) {
        OdinProjectCreationUtils.saveProjectSettings(project, settings);

        try {
            WriteAction.run(() -> {
                VirtualFile srcDir = OdinProjectCreationUtils.createSampleCode(this, baseDir);
                OdinProjectCreationUtils.setupNewModule(project, baseDir, srcDir);
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
        peer = new OdinProjectGeneratorPeer();
        return peer;
    }

    @Override
    public AbstractActionWithPanel createStep(DirectoryProjectGenerator<OdinProjectSettings> projectGenerator, AbstractNewProjectStep.AbstractCallback<OdinProjectSettings> callback) {
        return new OdinProjectSettingsStep(projectGenerator, new AbstractNewProjectStep.AbstractCallback<>());
    }
}
