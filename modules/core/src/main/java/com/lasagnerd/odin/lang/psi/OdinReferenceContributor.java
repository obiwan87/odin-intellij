package com.lasagnerd.odin.lang.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.lasagnerd.odin.codeInsight.imports.OdinImport;
import com.lasagnerd.odin.codeInsight.imports.OdinImportUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OdinReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(OdinImportPath.class),
                new OdinPackageReferenceProvider()
        );

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(OdinImportPath.class),
                new OdinCollectionReferenceProvider()
        );
    }

    public static class OdinPackageReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            if (element instanceof OdinImportPath odinImportPath) {
                OdinImport importInfo = OdinImportUtils.getImportInfo(odinImportPath);
                if(importInfo != null) {
                    Path path = Path.of(importInfo.path());

                    List<OdinPackageReference> packageReferences = new ArrayList<>();
                    int nameCount = path.getNameCount();
                    int caret = 0;
                    // quote
                    int offset = 1;
                    if(importInfo.collection() != null) {
                        offset += importInfo.collection().length()+1;
                    }
                    for (int i = 1; i <= nameCount; i++) {
                        Path subPath = path.subpath(0, i);
                        Path name = subPath.getName(i - 1);
                        TextRange textRange = TextRange.from(offset + caret, name.toString().length());
                        packageReferences.add(new OdinPackageReference(odinImportPath, textRange,
                                path,
                                subPath,
                                i-1));

                        // name + separator
                        caret += name.toString().length() + 1;
                    }


                    return packageReferences.toArray(new PsiReference[0]);
                }
            }
            return new PsiReference[0];
        }

        @Override
        public boolean acceptsHints(@NotNull PsiElement element,
                                    @NotNull PsiReferenceService.Hints hints) {
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

    private static class OdinCollectionReferenceProvider extends PsiReferenceProvider {
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
}
