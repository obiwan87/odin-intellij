package com.lasagnerd.odin.projectStructure.collection

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.lasagnerd.odin.codeInsight.imports.OdinCollection
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils.getVirtualFileManager
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType
import org.jetbrains.jps.model.module.JpsModuleSourceRootType
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors

class OdinJpsRootsService(val project: Project) : OdinRootsService {
    companion object {
        fun getCollectionFolder(directoryFile: VirtualFile, model: ModifiableRootModel): SourceFolder? {
            return getRootTypeSourceFolder(directoryFile, model, OdinCollectionRootType.INSTANCE)
        }

        fun getRootTypeSourceFolder(
            directoryFile: VirtualFile,
            model: ModifiableRootModel,
            vararg sourceRootType: JpsModuleSourceRootType<*>,
        ): SourceFolder? {
            val sourceRootTypes = Arrays.stream(sourceRootType).collect(Collectors.toSet())
            return Arrays.stream(model.contentEntries)
                .flatMap { c: ContentEntry -> c.getSourceFolders(sourceRootTypes).stream() }
                .filter { s: SourceFolder? -> s!!.file == directoryFile }
                .findFirst()
                .orElse(null)
        }
    }

    override fun findCollectionRoot(file: VirtualFile): OdinRootTypeResult? {
        val module = ModuleUtilCore.findModuleForFile(file, project)
        if (module != null) {
            val modifiableModel = ModuleRootManager.getInstance(module).modifiableModel
            val sourceFolder = getCollectionFolder(file, modifiableModel)
            if (sourceFolder != null) {
                return OdinJpsRootTypeResult(sourceFolder)
            }
        }
        return null

    }

    override fun findCollectionRoot(element: PsiElement, collectionName: String): OdinRootTypeResult? {
        val module = ModuleUtilCore.findModuleForPsiElement(element)
        if (module != null) {
            val modifiableModel = ModuleRootManager.getInstance(module).modifiableModel
            val sourceFolder = Arrays.stream(modifiableModel.contentEntries)
                .flatMap { c: ContentEntry -> c.getSourceFolders(OdinCollectionRootType.INSTANCE).stream() }.filter { s: SourceFolder? ->
                    val properties = s!!.jpsElement.properties
                    if (properties is OdinCollectionRootProperties) {
                        return@filter properties.collectionName == collectionName
                    }
                    false
                }.findFirst().orElse(null)

            if (sourceFolder != null) {
                return OdinJpsRootTypeResult(sourceFolder)
            }
        }
        return null

    }

    override fun findContainingCollection(virtualFile: VirtualFile): OdinRootTypeResult? {
        val sourceRootForFile = ProjectFileIndex.getInstance(project)
            .getSourceRootForFile(virtualFile)
        return findCollectionRoot(sourceRootForFile!!)
    }

    override fun findContainingRoot(virtualFile: VirtualFile): OdinRootTypeResult? {
        val file = ProjectFileIndex.getInstance(project)
            .getSourceRootForFile(virtualFile)

        if (file == null) return null

        val module = ModuleUtilCore.findModuleForFile(file, project)
        if (module != null) {
            val modifiableModel = ModuleRootManager.getInstance(module).modifiableModel
            val sourceFolder = getRootTypeSourceFolder(file, modifiableModel, OdinSourceRootType.INSTANCE, OdinCollectionRootType.INSTANCE)
            if (sourceFolder != null) {
                return OdinJpsRootTypeResult(sourceFolder)
            }
        }
        return null
    }

    override fun getCollection(directoryFile: VirtualFile): OdinCollection? {

        val module = ModuleUtilCore.findModuleForFile(directoryFile, project) ?: return null

        val sourceFolder = getCollectionFolder(
            directoryFile,
            ModuleRootManager.getInstance(module).modifiableModel
        )
        if (sourceFolder != null) {
            val properties = sourceFolder.jpsElement.properties
            if (properties is OdinCollectionRootProperties) {
                return OdinCollection(properties.collectionName, directoryFile.path)
            }
        }

        return null
    }

    override fun getCollectionPaths(sourceFilePath: String): Map<String, Path> {
        val collectionPaths: MutableMap<String, Path> = HashMap()
        val virtualFileManager = getVirtualFileManager() ?: return emptyMap()
        val sourceFile = virtualFileManager.findFileByNioPath(Path.of(sourceFilePath))
        if (sourceFile != null) {
            val module = ModuleUtilCore.findModuleForFile(sourceFile, project)
            if (module != null) {
                val model = ModuleRootManager.getInstance(module)
                val sourceFolders = Arrays.stream(model.contentEntries)
                    .flatMap { c: ContentEntry -> c.getSourceFolders(OdinCollectionRootType.INSTANCE).stream() }
                    .toList()

                for (sourceFolder in sourceFolders) {
                    val properties = sourceFolder.jpsElement.properties as OdinCollectionRootProperties
                    val collectionName = properties.collectionName
                    val collectionDirectory = sourceFolder.file
                    if (collectionDirectory != null) {
                        collectionPaths[collectionName!!] = collectionDirectory.toNioPath()
                    }
                }
            }
        }

        return collectionPaths

    }

    override fun renameCollection(collectionDirPath: VirtualFile, newName: String) {
        val module = ModuleUtilCore.findModuleForFile(collectionDirPath, project) ?: return

        val modifiableModel = ModuleRootManager.getInstance(module).modifiableModel
        val sourceFolder = getCollectionFolder(collectionDirPath, modifiableModel)
        if (sourceFolder != null) {
            if (modifiableModel.isDisposed) return

            val properties = sourceFolder.jpsElement.properties
            if (properties is OdinCollectionRootProperties) {
                properties.collectionName = newName
            }
            WriteAction.run<RuntimeException> { modifiableModel.commit() }
        }
    }

    override fun isCollectionRoot(file: VirtualFile): Boolean {
        if (file.isDirectory) {
            val result = findCollectionRoot(file) ?: return false
            return result.isCollectionRoot
        }

        return false
    }
}
