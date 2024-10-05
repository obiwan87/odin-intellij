package com.lasagnerd.odin.projectStructure

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class OdinProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        OdinProjectMigration.checkProject(project)
    }
}
