package com.lasagnerd.odin.projectStructure.module.rootTypes.collection;

import com.intellij.ide.projectView.actions.MarkSourceRootAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinMarkCollectionRootAction extends MarkSourceRootAction {
    public OdinMarkCollectionRootAction() {
        super(OdinCollectionRootType.INSTANCE);
    }

    @Override
    protected void doUpdate(@NotNull AnActionEvent e, @Nullable Module module, @NotNull RootsSelection selection) {
        super.doUpdate(e, module, selection);
    }

    @Override
    protected void modifyRoots(@NotNull VirtualFile vFile, @NotNull ContentEntry entry) {
        OdinCollectionRootProperties properties = new OdinCollectionRootProperties();
        properties.setCollectionName(vFile.getName());
        entry.addSourceFolder(vFile, OdinCollectionRootType.INSTANCE, properties);
    }
}
