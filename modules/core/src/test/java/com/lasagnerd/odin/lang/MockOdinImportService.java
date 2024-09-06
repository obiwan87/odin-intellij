package com.lasagnerd.odin.lang;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.testFramework.LightVirtualFile;
import com.lasagnerd.odin.codeInsight.imports.OdinImportService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockOdinImportService implements OdinImportService {

    private final String rootPath;
    private Map<String, String> nameToPathMap = new HashMap<>();
    private Map<String, VirtualFile> virtualFileMap = new HashMap<>();
    private final PsiFileFactoryImpl fileFactory;

    public MockOdinImportService(@NotNull PsiFileFactoryImpl fileFactory) {
        this.fileFactory = fileFactory;
        rootPath = "src/test/testData/";
        scanDirectories(rootPath);
    }

    private void scanDirectories(String rootPath) {
        File root = new File(rootPath);
        if (root.exists() && root.isDirectory()) {
            recursiveScan(root);
        }
    }

    private void recursiveScan(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                nameToPathMap.put(file.getName(), file.getAbsolutePath());
                recursiveScan(file);  // Recursively scan if it's a directory
            } else if (file.isFile() && file.getName().endsWith(".odin")) {
                nameToPathMap.put(file.getName(), file.getAbsolutePath());
            }
        }
    }

    @Override
    public String getPackagePath(PsiElement psiElement) {
        String name = psiElement.getContainingFile().getVirtualFile().getName();
        String path = nameToPathMap.get(name);
        return FileUtil.toSystemIndependentName(Path.of(path).getParent().toString());
    }

    @Override
    public @Nullable String getCanonicalPath(VirtualFile virtualFile) {
        return nameToPathMap.get(virtualFile.getName());
    }

    @Override
    public VirtualFile @NotNull [] getFilesInPath(Path importPath) {
        List<VirtualFile> virtualFiles = new ArrayList<>();
        Path absolutePath = importPath.toAbsolutePath();
        for (Map.Entry<String, String> entry : nameToPathMap.entrySet()) {
            Path filePath = Path.of(entry.getValue());
            Path parent = filePath.getParent();
            if (parent.equals(absolutePath) && filePath.toString().endsWith(".odin") && filePath.toFile().isFile()) {
                virtualFiles.add(MockFileUtils.createVirtualFile(filePath));
            }
        }
        return virtualFiles.toArray(new VirtualFile[0]);
    }

    @Override
    public PsiFile createPsiFile(VirtualFile virtualFile) {
        if (virtualFile instanceof LightVirtualFile lightVirtualFile) {
            return fileFactory.trySetupPsiForFile(lightVirtualFile, OdinLanguage.INSTANCE, true, false);
        }
        return null;
    }

}
