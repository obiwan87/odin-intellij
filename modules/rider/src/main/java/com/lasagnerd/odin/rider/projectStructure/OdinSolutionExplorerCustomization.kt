package com.lasagnerd.odin.rider.projectStructure

import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewSettings
import com.lasagnerd.odin.projectStructure.OdinTreeStructureProvider

class OdinSolutionExplorerCustomization(project: Project) : SolutionExplorerCustomization(project) {


    override fun modifyChildren(virtualFile: VirtualFile, settings: SolutionExplorerViewSettings, children: MutableList<AbstractTreeNode<*>>) {
        val newChildren = OdinTreeStructureProvider.modifyTreeStructureNodes(children)
        children.clear()
        children.addAll(newChildren)
    }
}
