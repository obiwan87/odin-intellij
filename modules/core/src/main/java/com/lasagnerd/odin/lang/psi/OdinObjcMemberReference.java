package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinObjcMemberReference extends PsiReferenceBase<OdinAttributeNamedValue> {

    public OdinObjcMemberReference(@NotNull OdinAttributeNamedValue element) {
        super(element);
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return TextRange.create(1, myElement.getTextLength() - 1);
    }
}
