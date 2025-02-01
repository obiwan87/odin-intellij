package com.lasagnerd.odin.projectStructure.collection

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.lasagnerd.odin.codeInsight.imports.OdinCollection
import com.lasagnerd.odin.riderInterop.OdinRiderInteropService
import java.nio.file.Path

interface OdinRootsService {
    companion object {

        var instance: OdinRootsService? = null

        @JvmStatic
        fun getInstance(project: Project): OdinRootsService {
            return if (OdinRiderInteropService.getInstance(project) != null) {
                OdinRiderInteropService.getInstance(project)
            } else {
                project.service<OdinJpsRootsService>()
            }
        }
    }

    fun findCollectionRoot(file: VirtualFile): OdinRootTypeResult?

    /**
     * Finds the sou
     */
    fun findCollectionRoot(element: PsiElement, collectionName: String): OdinRootTypeResult?

    fun findContainingCollection(virtualFile: VirtualFile): OdinRootTypeResult?

    fun findContainingRoot(virtualFile: VirtualFile): OdinRootTypeResult?

    fun getCollection(directoryFile: VirtualFile): OdinCollection?

    fun getCollectionPaths(sourceFilePath: String): Map<String, Path>

    fun renameCollection(collectionDirPath: VirtualFile, newName: String)

    fun isCollectionRoot(file: VirtualFile): Boolean
}
