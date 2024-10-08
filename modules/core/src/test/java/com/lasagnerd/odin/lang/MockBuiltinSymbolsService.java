package com.lasagnerd.odin.lang;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.lasagnerd.odin.codeInsight.symbols.OdinSdkServiceBase;
import com.lasagnerd.odin.lang.psi.OdinFile;

import java.nio.file.Path;
import java.util.Optional;

public class MockBuiltinSymbolsService extends OdinSdkServiceBase {
    private final PsiFileFactory fileFactory;

    public MockBuiltinSymbolsService(Project project, PsiFileFactory fileFactory) {
        super(project);
        this.fileFactory = fileFactory;
    }

    @Override
    protected OdinFile createOdinFile(Project project, Path path) {
        return MockFileUtils.createOdinFile(project, path);
    }

    @Override
    protected PsiFileFactory getPsiFileFactory(Project project) {
        return fileFactory;
    }

    @Override
    public Optional<String> getSdkPath() {
        return Optional.of("src/test/sdk");
    }
}
