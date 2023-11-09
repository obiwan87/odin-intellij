package com.lasagnerd.odin.lang.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.lasagnerd.odin.lang.OdinFileType;
import com.lasagnerd.odin.lang.OdinLanguage;
import org.jetbrains.annotations.NotNull;

public class OdinFile extends PsiFileBase {

    public static final String ODIN_FILE = "Odin File";

    public OdinFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, OdinLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return OdinFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return ODIN_FILE;
    }

    public OdinFileScope getFileScope() {
        return findChildByClass(OdinFileScope.class);
    }
}
