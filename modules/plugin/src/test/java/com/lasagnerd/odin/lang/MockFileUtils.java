package com.lasagnerd.odin.lang;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.testFramework.LightVirtualFile;
import com.lasagnerd.odin.lang.psi.OdinFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MockFileUtils {
    public static LightVirtualFile createVirtualFile(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return new LightVirtualFileWithPath(path.toFile().getName(), path.toAbsolutePath().toString(), OdinFileType.INSTANCE, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable OdinFile createOdinFile(Project project, Path path) {
        LightVirtualFile virtualFile = MockFileUtils.createVirtualFile(path);
        PsiFileFactory instance = project.getService(PsiFileFactory.class);
        if(instance instanceof PsiFileFactoryImpl fileFactory) {
            return (OdinFile) fileFactory.trySetupPsiForFile(virtualFile, OdinLanguage.INSTANCE, true, false);
        }
        return null;
    }
}
