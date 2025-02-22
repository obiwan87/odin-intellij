package com.lasagnerd.odin.projectStructure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.Gray;
import com.intellij.ui.SimpleTextAttributes;
import com.lasagnerd.odin.codeInsight.imports.OdinCollection;
import com.lasagnerd.odin.projectStructure.collection.OdinPsiCollection;
import com.lasagnerd.odin.projectStructure.collection.OdinPsiCollectionDirectory;
import com.lasagnerd.odin.projectStructure.collection.OdinRootsService;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OdinTreeStructureProvider implements TreeStructureProvider {
    private static @NotNull PsiDirectoryNode createCollectionDirectoryNode(PsiDirectoryNode directoryNode, OdinCollection collection, VirtualFile directoryFile) {
        String collectionName = collection.name();
        String directoryName = directoryFile.getName();

        OdinPsiCollection odinPsiCollection = new OdinPsiCollection(collectionName, directoryNode.getValue());
        OdinPsiCollectionDirectory odinPsiCollectionDirectory
                = new OdinPsiCollectionDirectory(directoryNode.getValue(), odinPsiCollection);
        PsiDirectoryNode newDirectoryNode = new PsiDirectoryNode(directoryNode.getProject(),
                odinPsiCollectionDirectory,
                // This is a workaround to #98: Compact middle packages hides content of collections
                // I am not sure why contents of collection roots are not rendered correctly when that
                // view setting is enabled. So to allow for correct work, we just do not hide middle packages.
                ViewSettings.DEFAULT
        );
        modifyPresentation(newDirectoryNode, directoryName, collectionName);
        return newDirectoryNode;
    }

    public static void modifyPresentation(AbstractTreeNode<?> newDirectoryNode, String directoryName, String collectionName) {
        PresentationData presentation = newDirectoryNode.getPresentation();
        modifyPresentation(presentation, directoryName, collectionName);
    }

    public static void modifyPresentation(PresentationData presentation, String directoryName, String collectionName) {
        Color foreground = presentation.getForcedTextForeground();

        if (directoryName.equals(collectionName)) {
            presentation.addText(directoryName,
                    new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, foreground)
            );
        } else {
            presentation.addText(directoryName,
                    new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, foreground)
            );
            presentation.addText(" [%s]".formatted(collectionName),
                    new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, Gray._150));
        }
    }

    public static @NotNull List<AbstractTreeNode<?>> modifyTreeStructureNodes(@NotNull Collection<AbstractTreeNode<?>> children) {
        List<AbstractTreeNode<?>> newChildren = new ArrayList<>(children.size() + 1);

        for (AbstractTreeNode<?> child : children) {
            if (!(child instanceof PsiDirectoryNode directoryNode) || directoryNode.getVirtualFile() == null) {
                newChildren.add(child);
                continue;
            }

            VirtualFile directoryFile = directoryNode.getVirtualFile();
            Project project = child.getProject();
            OdinCollection collection = OdinRootsService.getInstance(project).getCollection(directoryFile);
            if (collection != null) {
                PsiDirectoryNode newDirectoryNode = createCollectionDirectoryNode(directoryNode, collection, directoryFile);
                newChildren.add(newDirectoryNode);
                continue;
            }

            newChildren.add(child);
        }
        return newChildren;
    }

    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        return modifyTreeStructureNodes(children);
    }

    @Override
    public void uiDataSnapshot(@NotNull DataSink sink, @NotNull Collection<? extends AbstractTreeNode<?>> selection) {
        TreeStructureProvider.super.uiDataSnapshot(sink, selection);
    }
}
