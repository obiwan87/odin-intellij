package com.lasagnerd.odin.projectStructure.project;

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.platform.DirectoryProjectGenerator;
import com.lasagnerd.odin.projectSettings.OdinProjectSettings;

public class OdinProjectSettingsStep extends ProjectSettingsStepBase<OdinProjectSettings> {
    public OdinProjectSettingsStep(DirectoryProjectGenerator<OdinProjectSettings> projectGenerator, AbstractNewProjectStep.AbstractCallback<OdinProjectSettings> callback) {
        super(projectGenerator, callback);
    }
}
