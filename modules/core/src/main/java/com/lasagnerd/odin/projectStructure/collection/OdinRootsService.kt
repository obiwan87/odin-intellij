package com.lasagnerd.odin.projectStructure.collection

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.lasagnerd.odin.codeInsight.imports.OdinCollection
import com.lasagnerd.odin.rider.OdinRiderInteropService
import java.nio.file.Path

interface OdinRootsService {
    companion object {

        var instance: OdinRootsService? = null

        fun getInstance(project: Project): OdinRootsService {
            if (instance != null)
                return instance!!

            if (OdinRiderInteropService.getInstance(project) != null) {
                return OdinRiderInteropService.getInstance(project)
            } else {
                instance = OdinJpsRootsService(project)
            }

            throw NullPointerException("OdinRootsService must not be null")
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
