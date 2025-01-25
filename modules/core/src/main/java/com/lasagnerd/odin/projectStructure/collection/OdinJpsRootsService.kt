package com.lasagnerd.odin.projectStructure.collection

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.lasagnerd.odin.codeInsight.imports.OdinCollection
import com.lasagnerd.odin.projectStructure.collection.OdinRootsService.Companion.getInstance
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType
import com.lasagnerd.odin.projectStructure.module.rootTypes.source.OdinSourceRootType
import com.lasagnerd.odin.rider.OdinRiderInteropService
import org.jetbrains.jps.model.module.JpsModuleSourceRootType
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors

class OdinJpsRootsService(val project: Project) : OdinRootsService {
    object OdinJpsRootTypeUtils {
        private fun getCollectionFolder(directoryFile: VirtualFile, model: ModifiableRootModel): SourceFolder? {
            return getRootTypeSourceFolder(directoryFile, model, OdinCollectionRootType.INSTANCE)
        }

        private fun getRootTypeSourceFolder(
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

        /**
         * Check if the passed virtual file is a collection source root.
         *
         * @param project The project
         * @param file    The virtual file to check
         * @return A search results
         */
        fun findCollectionRoot(project: Project, file: VirtualFile): OdinRootTypeResult? {
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

        fun findCollectionRoot(element: PsiElement, collectionName: String): OdinRootTypeResult? {
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

        @JvmStatic
        fun findContainingCollection(project: Project, virtualFile: VirtualFile): OdinRootTypeResult? {
            val sourceRootForFile = ProjectFileIndex.getInstance(project)
                .getSourceRootForFile(virtualFile)

            return findCollectionRoot(project, sourceRootForFile!!)
        }

        @JvmStatic
        fun findContainingRoot(project: Project, virtualFile: VirtualFile): OdinRootTypeResult? {
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

        @JvmStatic
        fun getCollection(project: Project, directoryFile: VirtualFile): OdinCollection? {
            if (OdinRiderInteropService.isRider(project)) {
                return OdinRiderInteropService.getInstance(project).getCollection(directoryFile)
            }

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

        @JvmStatic
        fun getCollectionPaths(project: Project, sourceFilePath: String): Map<String, Path> {
            if (OdinRiderInteropService.isRider(project)) {
                val riderInteropService = OdinRiderInteropService.getInstance(project)
                return riderInteropService.collectionPaths
            }
            val collectionPaths: MutableMap<String, Path> = HashMap()
            val virtualFileManager = virtualFileManager ?: return emptyMap()
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

        @JvmStatic
        fun renameCollection(project: Project, collectionDirPath: VirtualFile, newName: String?) {
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

        @JvmStatic
        fun isCollectionRoot(project: Project, file: VirtualFile): Boolean {
            return getInstance(project).isCollectionRoot(file)
        }

        fun getCollectionPath(collectionName: String, psiElement: PsiElement): Path? {
            val containingFile = psiElement.originalElement.containingFile
            if (containingFile != null) {
                val virtualFile = containingFile.virtualFile
                if (virtualFile != null) {
                    val collectionPaths = getCollectionPaths(psiElement.project, virtualFile.path)
                    return collectionPaths[collectionName]
                }
            }
            return null
        }

        fun getCollectionName(project: Project, path: String): String? {
            val nioPath = Path.of(path)
            val collectionPaths = getCollectionPaths(project, path)
            for ((key, value) in collectionPaths) {
                if (value == nioPath) {
                    return key
                }
            }

            return null
        }

        @JvmStatic
        val virtualFileManager: VirtualFileManager?
            get() {
                return try {
                    VirtualFileManager.getInstance()
                } catch (e: Exception) {
                    null
                }
            }
    }

    override fun findCollectionRoot(file: VirtualFile): OdinRootTypeResult? {
        return OdinJpsRootTypeUtils.findCollectionRoot(project, file)
    }

    override fun findCollectionRoot(element: PsiElement, collectionName: String): OdinRootTypeResult? {
        return OdinJpsRootTypeUtils.findCollectionRoot(element, collectionName)
    }

    override fun findContainingCollection(virtualFile: VirtualFile): OdinRootTypeResult? {
        return OdinJpsRootTypeUtils.findContainingCollection(project, virtualFile)
    }

    override fun findContainingRoot(virtualFile: VirtualFile): OdinRootTypeResult? {
        return OdinJpsRootTypeUtils.findContainingRoot(project, virtualFile)
    }

    override fun getCollection(directoryFile: VirtualFile): OdinCollection? {
        return OdinJpsRootTypeUtils.getCollection(project, directoryFile)
    }

    override fun getCollectionPaths(sourceFilePath: String): Map<String, Path> {
        return OdinJpsRootTypeUtils.getCollectionPaths(project, sourceFilePath)
    }

    override fun renameCollection(collectionDirPath: VirtualFile, newName: String) {
        return OdinJpsRootTypeUtils.renameCollection(project, collectionDirPath, newName)
    }

    override fun isCollectionRoot(file: VirtualFile): Boolean {
        if (file.isDirectory) {
            val result = findCollectionRoot(file) ?: return false
            return result.isCollectionRoot
        }

        return false
    }
}
