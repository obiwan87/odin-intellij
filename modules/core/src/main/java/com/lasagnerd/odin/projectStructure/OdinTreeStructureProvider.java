package com.lasagnerd.odin.projectStructure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.Gray;
import com.intellij.ui.SimpleTextAttributes;
import com.lasagnerd.odin.projectStructure.collection.OdinPsiCollection;
import com.lasagnerd.odin.projectStructure.collection.OdinPsiCollectionDirectory;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OdinTreeStructureProvider implements TreeStructureProvider {
    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        List<AbstractTreeNode<?>> newChildren = new ArrayList<>(children.size() + 1);

        for (AbstractTreeNode<?> child : children) {
            if (child instanceof PsiDirectoryNode directoryNode) {
                VirtualFile directoryFile = directoryNode.getVirtualFile();
                if (directoryFile != null) {
                    Module module = ModuleUtilCore.findModuleForFile(directoryFile, child.getProject());
                    if (module == null) {
                        newChildren.add(child);
                        continue;
                    }

                    SourceFolder sourceFolder = OdinRootTypeUtils.getCollectionFolder(directoryFile,
                            ModuleRootManager.getInstance(module).getModifiableModel());
                    if (sourceFolder != null) {
                        JpsElement properties = sourceFolder.getJpsElement().getProperties();
                        if (properties instanceof OdinCollectionRootProperties collectionRootProperties) {

                            String collectionName = collectionRootProperties.getCollectionName();
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
//                                    directoryNode.getSettings()
                            );
                            PresentationData presentation = newDirectoryNode.getPresentation();
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
                            newChildren.add(newDirectoryNode);
                            continue;
                        }
                    }
                }
            }
            newChildren.add(child);
        }
        return newChildren;
    }

    @Override
    public void uiDataSnapshot(@NotNull DataSink sink, @NotNull Collection<? extends AbstractTreeNode<?>> selection) {
        TreeStructureProvider.super.uiDataSnapshot(sink, selection);
    }
}
