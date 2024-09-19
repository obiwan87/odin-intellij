package com.lasagnerd.odin.lang;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.testFramework.LightVirtualFile;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import com.lasagnerd.odin.lang.psi.OdinFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class MockOdinImportService implements OdinImportService {

    private final Map<String, LightVirtualFile> virtualFileMap = new HashMap<>();
    private final PsiFileFactoryImpl fileFactory;
    private final Map<String, OdinFile> pathToOdinFile = new HashMap<>();

    public MockOdinImportService(@NotNull PsiFileFactoryImpl fileFactory) {
        this.fileFactory = fileFactory;
    }

    @Override
    public String getPackagePath(PsiElement psiElement) {
        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        if(virtualFile != null) {
            String path = virtualFile.getPath();

        return FileUtil.toSystemIndependentName(Path.of(path).getParent().toString());
        }
        return null;
    }

    @Override
    public @Nullable String getCanonicalPath(VirtualFile virtualFile) {
        return virtualFile.getPath();
    }

    @Override
    public VirtualFile @NotNull [] getFilesInPath(Path importPath) {
        List<VirtualFile> virtualFiles = new ArrayList<>();

        File file = importPath.toFile();
        if (file.exists() && file.isDirectory()) {
            var files = file.listFiles((dir, name) -> name.endsWith(".odin"));
            if (files != null) {
                for (File odinFile : files) {
                    virtualFiles.add(getVirtualFile(odinFile.toPath()));
                }
            }
        }
        return virtualFiles.toArray(new VirtualFile[0]);
    }

    private @NotNull LightVirtualFile getVirtualFile(Path filePath) {
        String absolutePath = FileUtil.toSystemIndependentName(filePath.toAbsolutePath().toString());
        LightVirtualFile virtualFile = virtualFileMap.get(absolutePath);

        if (virtualFile == null) {
            virtualFile = MockFileUtils.createVirtualFile(filePath);
            virtualFileMap.put(absolutePath, virtualFile);
        }
        return Objects.requireNonNull(virtualFile);
    }

    @Override
    public PsiFile createPsiFile(VirtualFile virtualFile) {
        if (virtualFile instanceof LightVirtualFile lightVirtualFile) {
            String absolutePath = FileUtil.toSystemIndependentName(virtualFile.getPath());
            OdinFile odinFile = pathToOdinFile.get(absolutePath);
            if (odinFile == null) {
                odinFile = (OdinFile) fileFactory.trySetupPsiForFile(lightVirtualFile, OdinLanguage.INSTANCE, true, false);
                pathToOdinFile.put(absolutePath, odinFile);
            }

            return odinFile;
        }
        return null;
    }

    @Override
    public Optional<String> getSdkPath() {
        return Optional.of("src/test/sdk");
    }
}
