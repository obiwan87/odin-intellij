package com.lasagnerd.odin.rider

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.views.updateAllFromRoot

fun Project.updateSolutionView() {
    val projectView = ProjectView.getInstance(this)
    projectView?.updateAllFromRoot()
}