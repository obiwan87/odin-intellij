package com.lasagnerd.odin.projectStructure.module.rootTypes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.Gray;
import com.intellij.ui.SimpleTextAttributes;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootProperties;
import com.lasagnerd.odin.projectStructure.module.rootTypes.collection.OdinCollectionRootType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class OdinCollectionRootTreeStructureProvider implements TreeStructureProvider {
    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        for (AbstractTreeNode<?> child : children) {
            if (child instanceof PsiDirectoryNode directoryNode) {
                VirtualFile directoryFile = directoryNode.getVirtualFile();
                if (directoryFile != null) {
                    Module module = ModuleUtilCore.findModuleForFile(directoryNode.getVirtualFile(), parent.getProject());
                    if (module != null) {
                        ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
                        SourceFolder sourceFolder = Arrays.stream(model.getContentEntries())
                                .flatMap(c -> c.getSourceFolders(OdinCollectionRootType.INSTANCE).stream())
                                .filter(s -> Objects.equals(s.getFile(), directoryFile))
                                .findFirst()
                                .orElse(null);
                        if (sourceFolder == null)
                            continue;
                        JpsElement properties = sourceFolder.getJpsElement().getProperties();
                        if (properties instanceof OdinCollectionRootProperties collectionRootProperties) {
                            PresentationData presentation = child.getPresentation();

                            String collectionName = collectionRootProperties.getCollectionName();
                            String directoryName = directoryFile.getName();
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
                    }
                }
            }
        }
        return children;
    }
}
