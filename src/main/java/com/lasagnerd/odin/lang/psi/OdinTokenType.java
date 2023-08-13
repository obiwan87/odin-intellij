package com.lasagnerd.odin.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.lasagnerd.odin.lang.OdinLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class OdinTokenType extends IElementType {
    public OdinTokenType(@NonNls @NotNull String debugName) {
        super(debugName, OdinLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return OdinTokenType.class.getSimpleName() + "." + super.toString();
    }
}
