package com.lasagnerd.odin.rider.rootFolders

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.lasagnerd.odin.projectStructure.collection.OdinRootTypeResult

class OdinRiderRootTypeResult(private val project: Project, private val file: VirtualFile, private val name: String?) : OdinRootTypeResult {
    override fun collectionName(): String? {
        return name
    }

    override fun directory(): VirtualFile {
        return file
    }

    override fun isSourceRoot(): Boolean {
        return OdinRiderRootFoldersService.getInstance(project).state.sourceRoots.contains(file.path)
    }

    override fun isCollectionRoot(): Boolean {
        return OdinRiderRootFoldersService.getInstance(project).collcetionMap.containsKey(file.path)
    }
}