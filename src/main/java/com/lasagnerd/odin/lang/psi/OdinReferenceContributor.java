package com.lasagnerd.odin.lang.psi;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdinReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(OdinImportPath.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (element instanceof OdinImportPath odinImportPath) {
                            return new PsiReference[]{new OdinImportReference(odinImportPath)};
                        }
                        return new PsiReference[0];
                    }
                }
        );
    }
}
