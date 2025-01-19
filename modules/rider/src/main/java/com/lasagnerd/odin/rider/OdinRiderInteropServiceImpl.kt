package com.lasagnerd.odin.rider

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rider.projectView.views.updateAllFromRoot
import com.jetbrains.rider.workspaceModel.WorkspaceUserModelUpdater.Companion.getInstance
import com.lasagnerd.odin.codeInsight.imports.OdinCollection
import com.lasagnerd.odin.rider.rootFolders.OdinRootFoldersService
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
                updateProjectView(project)
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
                updateProjectView(project)
            }
        } catch (_: InvalidPathException) {
        }
    }

    private fun updateProjectView(project: Project) {
        val projectView = ProjectView.getInstance(project)
        projectView?.updateAllFromRoot()
    }

    override fun getCollection(directoryFile: VirtualFile): OdinCollection? {
        val name = OdinRootFoldersService.getInstance(project).state.collectionRoots[directoryFile.path]
        if (name != null) {
            return OdinCollection(directoryFile.path, name)
        }
        return null
    }

    override fun isSourceRoot(directoryFile: VirtualFile): Boolean {
        val path = directoryFile.path
        return path in OdinRootFoldersService.getInstance(project).state.sourceRoots
    }
}