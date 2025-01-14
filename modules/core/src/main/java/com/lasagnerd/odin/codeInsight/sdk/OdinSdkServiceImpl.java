package com.lasagnerd.odin.codeInsight.sdk;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.settings.projectSettings.OdinSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public class OdinSdkServiceImpl extends OdinSdkServiceBase {
    public OdinSdkServiceImpl(Project project) {
        super(project);
    }

    @Override
    protected OdinFile createOdinFile(Project project, Path path) {
        VirtualFile virtualFile = VfsUtil.findFile(path, true);
        if (virtualFile != null) {
            return (OdinFile) PsiManager.getInstance(project).findFile(virtualFile);
        }
        return null;
    }

    @Override
    protected PsiFileFactory getPsiFileFactory(Project project) {
        return PsiFileFactory.getInstance(project);
    }

    @Override
    public Optional<String> getSdkPath() {
        return OdinSdkUtils.getSdkPath(project);
    }

    @Override
    public void refreshCache() {

        ProgressManager.getInstance().run(new Backgroundable(project, "Loading odin built-in symbols", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ApplicationManager.getApplication().runReadAction(() -> {
                    // Perform caching logic

                    // TODO this shouldn't be necessary anymore once we have
                    //  stub indices.
                    invalidateCache();
                    loadBuiltinSymbols();
                });
            }
        });
    }

    @Override
    public VirtualFile getBuiltinVirtualFile() {
        return findSdkFile(Path.of("base", "builtin", "builtin.odin"));
    }

    @Override
    public VirtualFile getIntrinsicsFile() {
        return findSdkFile(Path.of("base", "intrinsics", "intrinsics.odin"));
    }

    private @Nullable VirtualFile findSdkFile(Path path) {
        Optional<String> validSdkPathOptional = OdinSdkUtils.getSdkPath(project);
        VirtualFile virtualFile;
        if (validSdkPathOptional.isPresent()) {
            String validSdkPath = validSdkPathOptional.get();
            Path builtinOdinPath = Path.of(validSdkPath).resolve(path);
            virtualFile = VirtualFileManager.getInstance().findFileByNioPath(builtinOdinPath);
        } else {
            virtualFile = null;
        }
        return virtualFile;
    }
}
