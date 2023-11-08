package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinReference extends PsiReferenceBase<PsiElement> {
    public OdinReference(@NotNull PsiElement element) {
        super(element);
    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return super.getAbsoluteRange();
    }

    @Override
    public @Nullable PsiElement resolve() {
        var element = getElement();
        return OdinInsightUtils.findFirstDeclaration(element, psi -> psi.getText().equals(element.getText()));
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }
}
