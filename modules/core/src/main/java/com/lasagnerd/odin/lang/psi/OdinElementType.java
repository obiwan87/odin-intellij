package com.lasagnerd.odin.lang.psi;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.OdinLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinElementType extends IElementType {
    public OdinElementType(@NonNls @NotNull String debugName) {
        super(debugName, OdinLanguage.INSTANCE);
    }
}
