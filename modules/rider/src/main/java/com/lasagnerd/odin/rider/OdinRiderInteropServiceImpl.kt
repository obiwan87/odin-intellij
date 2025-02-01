package com.lasagnerd.odin.rider

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.PsiElement
import com.intellij.util.containers.reverse
import com.jetbrains.rider.projectView.views.updateAllFromRoot
import com.jetbrains.rider.workspaceModel.WorkspaceUserModelUpdater.Companion.getInstance
import com.lasagnerd.odin.codeInsight.imports.OdinCollection
import com.lasagnerd.odin.projectStructure.collection.OdinRootTypeResult
import com.lasagnerd.odin.rider.rootFolders.OdinRiderRootFoldersService
import com.lasagnerd.odin.rider.rootFolders.OdinRiderRootTypeResult
import com.lasagnerd.odin.riderInterop.OdinRiderInteropService
import okio.FileNotFoundException
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

    override fun findContainingRoot(virtualFile: VirtualFile): OdinRootTypeResult? {
        val rootFoldersService = OdinRiderRootFoldersService.getInstance(project)
        val sourceRootPath = rootFoldersService.state.sourceRoots.firstOrNull {
            virtualFile.path.startsWith(it)
        }
        if (sourceRootPath != null) {
            val rootVirtualFile = VirtualFileManager.getInstance()
                .findFileByNioPath(sourceRootPath.toNioPathOrNull() ?: throw IllegalArgumentException("File has no path"))
                ?: throw FileNotFoundException("Root was not found")

            return OdinRiderRootTypeResult(project, rootVirtualFile, null)
        }

        val collectionRootPath = rootFoldersService.state.collectionRoots.keys.firstOrNull {

            virtualFile.path.startsWith(it)
        }

        if (collectionRootPath != null) {
            val rootVirtualFile = VirtualFileManager.getInstance()
                .findFileByNioPath(collectionRootPath.toNioPathOrNull() ?: throw IllegalArgumentException("File has no path"))
                ?: throw FileNotFoundException("Root file was not found")
            val collectionName = rootFoldersService.state.collectionRoots[collectionRootPath]
            return OdinRiderRootTypeResult(project, rootVirtualFile, collectionName)
        }

        return null
    }

    override fun isCollectionRoot(file: VirtualFile): Boolean {
        val rootFoldersService = OdinRiderRootFoldersService.getInstance(project)
        return rootFoldersService.isCollectionRoot(file)
    }

    override fun findCollectionRoot(file: VirtualFile): OdinRootTypeResult? {
        val rootFoldersService = OdinRiderRootFoldersService.getInstance(project)
        val collection = rootFoldersService.collcetionMap[file.path]
        return collection?.let {
            return OdinRiderRootTypeResult(project, file, collection.name)
        }
    }

    override fun findCollectionRoot(element: PsiElement, collectionName: String): OdinRootTypeResult? {
        val rootFoldersService = OdinRiderRootFoldersService.getInstance(project)
        val collectionPath = rootFoldersService.state.collectionRoots.reverse()[collectionName]?.toNioPathOrNull()

        if (collectionPath != null) {
            val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(collectionPath)
            if (virtualFile != null) {
                return OdinRiderRootTypeResult(project, virtualFile, collectionName)
            }
        }
        return null
    }


    override fun renameCollection(collectionDirPath: VirtualFile, newName: String) {
        val rootFoldersService = OdinRiderRootFoldersService.getInstance(project)
        val path = collectionDirPath.path
        rootFoldersService.state.collectionRoots[path]
            ?: throw IllegalArgumentException("Collection with path $path does not exist!")
        rootFoldersService.state.collectionRoots[path] = newName

    }

    override fun getCollectionPaths(sourceFilePath: String): Map<String, Path> {
        // TODO(lasagnerd) Check if there is duplicate collection names
        val reversedMap = OdinRiderRootFoldersService.getInstance(project).state.collectionRoots.reverse()
            .mapValues { it.value.toNioPathOrNull() ?: throw NullPointerException("Invalid path") }

        return reversedMap
    }

    override fun findContainingCollection(virtualFile: VirtualFile): OdinRootTypeResult? {
        val rootFoldersService = OdinRiderRootFoldersService.getInstance(project)
        return rootFoldersService
            .state.collectionRoots.keys.firstOrNull {
                virtualFile.path.startsWith(it)
            }.let {
                if (it != null) {
                    val rootVirtualFile =
                        VirtualFileManager.getInstance().findFileByNioPath(it.toNioPathOrNull() ?: throw IllegalStateException("Path not available"))
                            ?: throw FileNotFoundException("Root not found")
                    return OdinRiderRootTypeResult(project, rootVirtualFile, rootFoldersService.state.collectionRoots[it]!!)
                }

                null
            }
    }

    override fun getCollection(directoryFile: VirtualFile): OdinCollection? {
        val name = OdinRiderRootFoldersService.getInstance(project).state.collectionRoots[directoryFile.path]
        if (name != null) {
            return OdinCollection(directoryFile.path, name)
        }
        return null
    }

    override fun isSourceRoot(directoryFile: VirtualFile): Boolean {
        val path = directoryFile.path
        return path in OdinRiderRootFoldersService.getInstance(project).state.sourceRoots
    }

    override fun isUnderRoot(virtualFile: VirtualFile): Boolean {
        val nioPath = virtualFile.toNioPathOrNull()
        if (nioPath != null) {
            return OdinRiderRootFoldersService.getInstance(project).rootPaths.any {
                nioPath.startsWith(it)
            }
        }
        return false
    }

    override fun getCollectionPaths(): MutableMap<String, Path> {
        val map = mutableMapOf<String, Path>()
        for (entry in OdinRiderRootFoldersService.getInstance(project).collcetionMap.entries) {
            val nioPath = entry.value.path.toNioPathOrNull()
            if (nioPath != null) {
                map[entry.value.name] = nioPath
            }
        }
        return map
    }


}