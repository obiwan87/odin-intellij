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
                            return new PsiReference[]{new OdinPackageReference(odinImportPath)};
                        }
                        return new PsiReference[0];
                    }

                    @Override
                    public boolean acceptsHints(@NotNull PsiElement element, PsiReferenceService.@NotNull Hints hints) {
                        if (hints.offsetInElement != null) {
                            String text = element.getText();
                            int index = text.indexOf(':');
                            if (index < 0) {
                                return super.acceptsHints(element, hints);
                            }

                            if (hints.offsetInElement <= index) {
                                return false;
                            }
                        }
                        return super.acceptsHints(element, hints);
                    }
                }
        );

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(OdinImportPath.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (element instanceof OdinImportPath odinImportPath) {
                            return new PsiReference[]{new OdinCollectionReference(odinImportPath)};
                        }
                        return new PsiReference[0];
                    }

                    @Override
                    public boolean acceptsHints(@NotNull PsiElement element, PsiReferenceService.@NotNull Hints hints) {
                        String text = element.getText();
                        int index = text.indexOf(':');
                        if (index < 0) {
                            return false;
                        }

                        if (hints.offsetInElement == null) {
                            return super.acceptsHints(element, hints);
                        }

                        return hints.offsetInElement < index;
                    }
                }
        );
    }
}
