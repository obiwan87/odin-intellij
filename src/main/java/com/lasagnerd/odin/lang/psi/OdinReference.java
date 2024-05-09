package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.lasagnerd.odin.insights.OdinInsightUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdinReference extends PsiReferenceBase<OdinIdentifier> {
    public OdinReference(@NotNull OdinIdentifier element) {
        super(element);
    }

    @Override
    public @NotNull TextRange getAbsoluteRange() {
        return super.getAbsoluteRange();
    }

    @Override
    public @Nullable PsiElement resolve() {
        System.out.println("Resolving reference of " + getElement().getText());
        return OdinInsightUtils.findFirstDeclaration(getElement());
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }
}
