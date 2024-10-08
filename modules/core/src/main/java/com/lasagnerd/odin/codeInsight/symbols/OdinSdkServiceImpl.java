package com.lasagnerd.odin.codeInsight.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.lasagnerd.odin.lang.psi.OdinFile;
import com.lasagnerd.odin.projectSettings.OdinSdkUtils;

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
}
