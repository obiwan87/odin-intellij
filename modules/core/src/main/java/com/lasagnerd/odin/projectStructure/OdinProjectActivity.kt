package com.lasagnerd.odin.projectStructure

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkService

class OdinProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().runReadAction {
            OdinSdkService.getInstance(project).refreshCache();
        }
        OdinProjectMigration.checkProject(project)
    }
}
