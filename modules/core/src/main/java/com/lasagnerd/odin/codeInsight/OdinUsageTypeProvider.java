package com.lasagnerd.odin.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO implement
public class OdinUsageTypeProvider implements UsageTypeProvider {
    @Override
    public @Nullable UsageType getUsageType(@NotNull PsiElement psiElement) {
        return null;
    }
}
