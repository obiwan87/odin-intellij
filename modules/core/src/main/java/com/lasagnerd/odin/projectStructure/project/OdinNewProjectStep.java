package com.lasagnerd.odin.projectStructure.project;

import com.intellij.ide.wizard.AbstractNewProjectWizardStep;
import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.dsl.builder.AlignX;
import com.intellij.ui.dsl.builder.Panel;
import com.lasagnerd.odin.projectSettings.OdinProjectSettings;
import com.lasagnerd.odin.projectStructure.module.OdinModuleType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

public class OdinNewProjectStep extends AbstractNewProjectWizardStep {
    private final OdinProjectSettings projectSettings = new OdinProjectSettings();

    public OdinNewProjectStep(@NotNull NewProjectWizardStep parentStep) {
        super(parentStep);
    }

    @Override
    public void setupUI(@NotNull Panel builder) {
        JComponent component = projectSettings.getComponent();
        builder.row((JLabel) null, r -> {
            r.cell(component).align(AlignX.FILL);
            return null;
        });
    }

    @Override
    public void setupProject(@NotNull Project project) {
        super.setupProject(project);

        OdinProjectCreationUtils.saveProjectSettings(project, projectSettings);

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        VirtualFile workspaceFile = project.getWorkspaceFile();
        if (workspaceFile == null) {
            return;
        }

        String basePath = project.getBasePath();
        if (basePath == null)
            return;

        VirtualFile baseDir = VirtualFileManager.getInstance().findFileByNioPath(Path.of(basePath));
        if (baseDir == null)
            return;

        Path path = Path.of(workspaceFile.getParent().getPath(), project.getName());
        try {
            WriteAction.run(() -> {
                Module module = moduleManager.newModule(path, OdinModuleType.ODIN_MODULE);
                VirtualFile srcDir = OdinProjectCreationUtils.createSampleCode(this, baseDir);
                OdinProjectCreationUtils.setupModule(baseDir, srcDir, module);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
