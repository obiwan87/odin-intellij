package com.lasagnerd.odin.rider;

import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.ide.projectView.actions.MarkSourceRootAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.roots.ui.configuration.ModuleSourceRootEditHandler;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.util.Iterator;

public abstract class OdinRiderMarkRootAction extends MarkSourceRootAction {
    private final JpsModuleSourceRootType<?> rootType;

    public OdinRiderMarkRootAction(@NotNull JpsModuleSourceRootType<?> rootType) {
        super(rootType);
        this.rootType = rootType;
    }

    @Override
    protected boolean isEnabled(@NotNull RootsSelection selection, @NotNull Module module) {
        ModuleType<?> moduleType = ModuleType.get(module);
        if (ModuleSourceRootEditHandler.getEditHandler(this.rootType) != null && (!selection.myHaveSelectedFilesUnderSourceRoots || moduleType.isMarkInnerSupportedFor(this.rootType))) {
            if (selection.mySelectedDirectories.isEmpty()) {
                Iterator<SourceFolder> iterator = selection.mySelectedRoots.iterator();
                SourceFolder root;
                do {
                    if (!iterator.hasNext()) {
                        return false;
                    }

                    root = iterator.next();
                } while (this.rootType.equals(root.getRootType()));

            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        super.actionPerformed(e);
        refresh();
    }

    public static void refresh() {
        FileDocumentManager.getInstance().saveAllDocuments();
        SaveAndSyncHandler.getInstance().refreshOpenFiles();
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
    }
}
