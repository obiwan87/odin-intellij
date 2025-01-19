package com.lasagnerd.odin.rider

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.views.updateAllFromRoot
import com.jetbrains.rider.workspaceModel.WorkspaceUserModelUpdater.Companion.getInstance
import java.nio.file.InvalidPathException
import java.nio.file.Path

class OdinRiderInteropServiceImpl(private val project: Project) : OdinRiderInteropService {
    override fun attachSdkRoot(path: String) {
        try {
            val workspaceUserModelUpdater = getInstance(project)
            val nioPath = Path.of(path)
            val file = nioPath.toFile()
            if (!workspaceUserModelUpdater.isAttachedFolder(file)) {
                workspaceUserModelUpdater.attachFolder(file)
            }
        } catch (_: InvalidPathException) {
        }
    }

    override fun detachSdkRoot(path: String) {
        try {
            val workspaceUserModelUpdater = getInstance(project)
            val nioPath = Path.of(path)
            val file = nioPath.toFile()
            if (workspaceUserModelUpdater.isAttachedFolder(file)) {
                workspaceUserModelUpdater.detachFolder(file)
            }
            val projectView = ProjectView.getInstance(project)
            projectView?.updateAllFromRoot()

        } catch (_: InvalidPathException) {
        }
    }
}
