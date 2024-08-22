package com.lasagnerd.odin.externalBuildSystem

import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectTracker
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class OdinProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        ExternalSystemProjectTracker.getInstance(project).register(
            OdinExternalSystemProjectAware(project)
        );
    }
}
