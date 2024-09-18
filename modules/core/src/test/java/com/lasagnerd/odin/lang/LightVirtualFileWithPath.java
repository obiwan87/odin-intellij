package com.lasagnerd.odin.lang;


import com.intellij.openapi.fileTypes.FileType;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

public class LightVirtualFileWithPath extends LightVirtualFile {
    private final String path;

    public LightVirtualFileWithPath(@NotNull String name, String path, FileType fileType, @NotNull CharSequence text) {
        super(name, fileType, text);
        this.path = path;
    }

    @Override
    @NotNull
    public String getPath() {
        return path;
    }
}
