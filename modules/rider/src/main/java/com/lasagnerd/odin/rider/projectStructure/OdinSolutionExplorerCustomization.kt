package com.lasagnerd.odin.rider.projectStructure

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewSettings
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.lasagnerd.odin.projectStructure.OdinTreeStructureProvider
import com.lasagnerd.odin.projectStructure.collection.OdinRootsService

class OdinSolutionExplorerCustomization(project: Project) : SolutionExplorerCustomization(project) {

    override fun modifyChildren(virtualFile: VirtualFile, settings: SolutionExplorerViewSettings, children: MutableList<AbstractTreeNode<*>>) {

    }

    override fun updateNode(presentation: PresentationData, virtualFile: VirtualFile) {
        val rootsService = OdinRootsService.getInstance(project)

        val collection = rootsService.getCollection(virtualFile)
        if (collection != null) {
            presentation.clearText()
            OdinTreeStructureProvider.modifyPresentation(presentation, virtualFile.name, collection.name)
        }
        super.updateNode(presentation, virtualFile)
    }

    override fun updateNode(presentation: PresentationData, entity: ProjectModelEntity) {
        super.updateNode(presentation, entity)
    }


}
