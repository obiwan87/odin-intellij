package com.lasagnerd.odin.projectStructure.library;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VirtualFile;
import com.lasagnerd.odin.OdinIcons;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class OdinSdkLibrary extends SyntheticLibrary implements ItemPresentation {

    private final VirtualFile baseDir;

    public OdinSdkLibrary(VirtualFile baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public @Nullable String getPresentableText() {
        return "Odin SDK";
    }

    @Override
    public @Nullable Icon getIcon(boolean unused) {
        return OdinIcons.OdinSdk;
    }

    @Override
    public @NotNull Collection<VirtualFile> getSourceRoots() {
        return List.of(baseDir);
    }
}
