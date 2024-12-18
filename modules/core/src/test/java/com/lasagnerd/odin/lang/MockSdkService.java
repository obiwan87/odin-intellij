package com.lasagnerd.odin.lang;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileFactory;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.codeInsight.sdk.OdinSdkServiceBase;
import com.lasagnerd.odin.lang.psi.OdinFile;

import java.nio.file.Path;
import java.util.Optional;

public class MockSdkService extends OdinSdkServiceBase {
    private final PsiFileFactory fileFactory;

    public MockSdkService(Project project, PsiFileFactory fileFactory) {
        super(project);
        this.fileFactory = fileFactory;
    }

    @Override
    protected OdinFile createOdinFile(Project project, Path path) {
        MockOdinImportService mockImportService = (MockOdinImportService) OdinImportService.getInstance(project);
        return (OdinFile) mockImportService.getPsiFile(path);
    }

    @Override
    protected PsiFileFactory getPsiFileFactory(Project project) {
        return fileFactory;
    }

    @Override
    public Optional<String> getSdkPath() {
        return Optional.of("src/test/sdk");
    }

    @Override
    public void refreshCache() {

    }

    @Override
    public VirtualFile getBuiltinVirtualFile() {
        Path path = Path.of(getSdkPath().get(), "base", "builtin", "builtin.odin");
        MockOdinImportService mockImportService = (MockOdinImportService) OdinImportService.getInstance(project);
        return mockImportService.getVirtualFile(path);
    }

    @Override
    public VirtualFile getIntrinsicsFile() {
        Path path = Path.of(getSdkPath().get(), "base", "intrinsics", "intrinsics.odin");
        MockOdinImportService mockImportService = (MockOdinImportService) OdinImportService.getInstance(project);
        return mockImportService.getVirtualFile(path);
    }
}
